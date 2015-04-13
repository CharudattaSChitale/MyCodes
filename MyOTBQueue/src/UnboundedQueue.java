import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;





public class UnboundedQueue implements myQueueSet {
	/**
	   * Lock out other enqueuers (dequeuers)
	   */
	  ReentrantLock enqLock, deqLock;
	  /**
	   * First entry in queue.
	   */
	  Node head;
	  /**
	   * Last entry in queue.
	   */
	  Node tail;
	  /**
	   * Queue size.
	   */
	  int size;
	  /**
	   * Constructor.
	   */
	  public UnboundedQueue() {
	    head = new Node();
	    tail = head;
	    enqLock = new ReentrantLock();
	    deqLock = new ReentrantLock();
	  }  
	  /**
	   * @return remove first item in queue
	   * @throws queue.EmptyException 
	   * 
	   */
	  public boolean deq() throws AbortedException {
	    int result;
	    deqLock.lock();
	    try {
	      if (head.next == null) {
	        return false;
	      }
	      result = head.next.value;
	      ArrayList list=((SetThread)Thread.currentThread()).list;
			list.add(result);
	      head = head.next;
	    } finally {
	      deqLock.unlock();
	    }
	    return true;
	  }
	  /**
	   * Appende item to end of queue.
	   * @param x item to append
	   */
	  public boolean enq(int x) throws AbortedException {
	    enqLock.lock();
	    try {
	      Node e = new Node(x);
	      tail.next = e;
	      tail = e;
	    } finally {
	      enqLock.unlock();
	    }
	    return true;
	  }
	
	@Override
	public boolean nontransactionalEnq(int x) {
		 enqLock.lock();
		    try {
		      Node e = new Node(x);
		      tail.next = e;
		      tail = e;
		    } finally {
		      enqLock.unlock();
		    }
		    return true;
	}
	@Override
	public void abort() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void commit() throws AbortedException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Node getHead() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Node getTail() {
		// TODO Auto-generated method stub
		return null;
	}

	  /**
	   * Individual queue item.
	   */
}
