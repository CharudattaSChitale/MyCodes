import java.util.ArrayList;




public class OptimisticBoostedQueue implements myQueueSet {
	Node head,tail;
	
	final int ENQ = 0;
	final int DEQ = 1;
	
	public OptimisticBoostedQueue(){
		head=new Node();
		tail=head;
	}
	
    public Node getHead(){
	return head;
    }
    
    public Node getTail(){
    	return tail;
    }
	@Override
	public boolean enq(int item) throws AbortedException {
		QueueLinkedList writeQueue=((SetThread)Thread.currentThread()).writeQueueThread;
		//System.out.println("inside enq");
		writeQueue.enqueue(item);
		//System.out.println("item added in local queue");
		if(!Validate())
    	{
		//System.out.println("item invalidated");
    	throw AbortedException.abortedException;
    	}
		//System.out.println("item validated");
		System.out.println("successful enq");
		return true;
		
	}

	public boolean Validate(){
		//System.out.println("current head-"+head);
		//System.out.println("original head"+((SetThread)Thread.currentThread()).deqReadSet);
		if(head!=((SetThread)Thread.currentThread()).deqReadSet || tail!=((SetThread)Thread.currentThread()).enqReadSet)
		{
			//System.out.println("tail or head changed");
			return false;
		}
		else
		{
		//System.out.println("validation succedded");
		return true;
		}
	}
	@Override
	public boolean deq() throws AbortedException {
		if(head.next==null)
    		return false;
		Node localHead=((SetThread)Thread.currentThread()).localHead;
		ArrayList list=((SetThread)Thread.currentThread()).list;
		//((SetThread)Thread.currentThread()).list.add(localHead.next);
		list.add(localHead.next.value);
		localHead=localHead.next;
		
		if(!Validate())
			throw AbortedException.abortedException;
		return true;
	}

	@Override
	public void commit() throws AbortedException {
		//System.out.println("inside commit");
		long threadId=Thread.currentThread().getId();
		int headLock=head.lock.get();
		int tailLock=tail.lock.get();
    	//abort if lock acquired by someone else
		if(!Validate())
    		throw AbortedException.abortedException;
    	if(((headLock & 1)==1 && head.lockHolder!=threadId) || ((tailLock & 1)==1 && tail.lockHolder!=threadId))
    		throw AbortedException.abortedException;
    	//acquire lock
    	if(head.lockHolder == threadId || head.lock.compareAndSet(headLock, headLock+1))
  	    	
  	    	head.lockHolder=threadId;
    	
    	if(tail.lockHolder==threadId || tail.lock.compareAndSet(tailLock, tailLock+1))
    		
    		tail.lockHolder=threadId;
    	//commit enq and deq after acquiring the locks
    	tail.next=((SetThread)Thread.currentThread()).writeQueueThread.head.next;
    	tail=((SetThread)Thread.currentThread()).writeQueueThread.tail;
    	
    	head=((SetThread)Thread.currentThread()).localHead;
    	
    	//unlock
    	//unlock head and tail
    	if(head.lockHolder == threadId)
		{
    		head.lockHolder = -1;
    		head.lock.incrementAndGet();
		}
    	if(tail.lockHolder == threadId)
		{
    		tail.lockHolder = -1;
    		tail.lock.incrementAndGet();
		}
	}
	
	

	@Override
	public boolean nontransactionalEnq(int item) {
		Node n=new Node(item);
    	tail.next=n;
    	tail=n;
    	//System.out.println("non-transactionally added");
    return true;
		
	}

	@Override
	public void abort() {
		long threadId = ((SetThread) Thread.currentThread()).getId();
		if(head.lockHolder == threadId)
		{
    		head.lockHolder = -1;
    		head.lock.incrementAndGet();
		}
		if(tail.lockHolder == threadId)
		{
    		tail.lockHolder = -1;
    		tail.lock.incrementAndGet();
		}
	}

}
