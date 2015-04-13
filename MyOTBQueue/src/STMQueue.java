import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.deuce.Atomic;


public class STMQueue implements myQueueSet {

	Node head;
	  /**
	   * Last entry in queue.
	   */
	  Node tail;
	  /**
	   * Queue size.
	   */
	  int size;
	  public STMQueue() {
		    head = new Node();
		    tail = head;
		    
		  } 
	@Atomic
	public boolean enq(int item) throws AbortedException {
		
		      Node e = new Node(item);
		      tail.next = e;
		      tail = e;
		    
	    
	    return true;
	}

	@Atomic
	public boolean deq() throws AbortedException {
		int result;
		System.out.println("head.next:"+head.next);
	   if (head.next == null) {
	        return false;
	      }
	      result = head.next.value;
	      ArrayList list=((SetThread)Thread.currentThread()).list;
			list.add(result);
	      head = head.next;
	    
	    return true;
	    
	     
	}

	@Override
	public void commit() throws AbortedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean nontransactionalEnq(int item) {
		 Node e = new Node(item);
	      tail.next = e;
	      tail = e;
	   // System.out.println("head.next"+head.next);
   
   return true;
	}

	@Override
	public void abort() {
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

}
