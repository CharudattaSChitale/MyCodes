

/*
 * BucketList.java
 *
 * Created on December 30, 2005, 3:24 PM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */












import java.util.Map.Entry;
import java.util.concurrent.atomic.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;





/**
 * @param T item type
 * @author Maurice Herlihy
 */
public class OptimisticBoostedBucketList implements IntSet {
  static final int WORD_SIZE = 24;
  static final int LO_MASK = 0x00000001;
  static final int HI_MASK = 0x00800000;
  static final int MASK = 0x00FFFFFF;
  final int ADD = 0;
  final int REMOVE = 1;
  final int CONTAINS = 2;
  Node head;
  /**
   * Constructor
   */
  class ReadSetEntry {
		Node pred;
		Node curr;
		boolean checkLink;
		
		public ReadSetEntry(Node pred, Node curr, boolean checkLink) {
			this.pred = pred;
			this.curr = curr;
			this.checkLink = checkLink;
		}	
	}
	
	class WriteSetEntry {
		int item;
		Node pred;
		Node curr;
		Node newNode;
		int operation;
		
		public WriteSetEntry(Node pred, Node curr, int operation, int item) {
			this.pred = pred;
			this.curr = curr;
			this.operation = operation;
			this.item = item;
		}
	}
  public OptimisticBoostedBucketList() {
	  Node n;
    this.head = new Node(0);
    this.head.next =
        new AtomicMarkableReference<Node>(new Node(Integer.MAX_VALUE), false);
  }
  private OptimisticBoostedBucketList(Node e) {
    this.head  = e;
  }
  /**
   * Restricted-size hash code
   * @param x object to hash
   * @return hash code
   */
  public static int hashCode(Object x) {
    return x.hashCode() & MASK;
  }
  public boolean add(int x) throws AbortedException{
	  
   int key = makeRegularKey(x);
   return operation(ADD,key);
   /* boolean splice;
    while (true) {
      // find predecessor and current entries
      Window window = find(head, key);
      Node pred = window.pred;
      Node curr = window.curr;
      // is the key present?
      if (curr.key == key) {
        return false;
      } else {
        // splice in new entry
        Node entry = new Node(key, x);
        entry.next.set(curr, false);
        splice = pred.next.compareAndSet(curr, entry, false, false);
        if (splice)
          return true;
        else
          continue;
      }
    }*/
  }
  public boolean remove(int x) throws AbortedException{
	  //System.out.print("inside remove");
	  int key = makeRegularKey(x);
	  if(!operation(REMOVE,key)){
		//  System.out.println("cant remove");
		  return false;
	  }
	  //System.out.println("can be removed");
	  return true;
  }
    /*int key = makeRegularKey(x);
    boolean snip;
    while (true) {
      // find predecessor and current entries
      Window window = find(head, key);
      Node pred = window.pred;
      Node curr = window.curr;
      // is the key present?
      if (curr.key != key) {
        return false;
      } else {
        // snip out matching entry
        snip = pred.next.attemptMark(curr, true);
        if (snip)
          return true;
        else
          continue;
      }
    }
  }*/
  public boolean contains(int x) throws AbortedException{
	  int key = makeRegularKey(x);
	  return operation(CONTAINS,key) ;
    /*int key = makeRegularKey(x);
    Window window = find(head, key);
    Node pred = window.pred;
    Node curr = window.curr;
    return (curr.key == key);*/
  }
  
	private boolean postValidate(ArrayList<ReadSetEntry> readset) {
		ReadSetEntry entry;	
		int size = readset.size();

		int [] predLocks = new int[size];
		int [] currLocks = new int[size];
		
		// get snapshot of lock values
		for(int i=0; i<size; i++)
		{
			entry = readset.get(i);
			predLocks[i] = entry.pred.lock.get();
			currLocks[i] = entry.curr.lock.get();
		}
		
		// check the values of the nodes 
		// and also check that nodes are not currently locked
		for(int i=0; i<size; i++)
		{
			entry = readset.get(i);
			if((currLocks[i] & 1) == 1 || entry.curr.marked)
				return false;
			if(entry.checkLink)
			{
				if((predLocks[i] & 1) == 1 || entry.pred.marked || entry.curr != entry.pred.next.getReference()) 
					return false;
			}
		}
		
		// check that lock values are still the same since validation starts
		for(int i=0; i<size; i++)
		{
			entry = readset.get(i);
			if(currLocks[i] != entry.curr.lock.get())
				return false;
			if(entry.checkLink && predLocks[i] != entry.pred.lock.get())
				return false;
		}
		return true;
	}
  
  public boolean operation(int type, int item) throws AbortedException	{
		TreeMap<Integer,WriteSetEntry> writeset = ((SetThread) Thread.currentThread()).list_writeset;
		
		WriteSetEntry entry = writeset.get(item);
		if(entry != null)
		{
			if(entry.operation == ADD)
			{
				if(type == ADD) return false;
				else if(type == CONTAINS) return true;
				else // remove
				{
//					System.out.println("remove after add");
					writeset.remove(item);
					//System.out.println("removed successfully otb");
					//System.out.print("can remove");
					return true;
				}
			}
			else // remove
			{//doubt
				if(type == REMOVE || type == CONTAINS)
					return false;
				else // add
				{
//					System.out.println("add after remove");
					writeset.remove(item);
					return true;
				}
			}
		}
	
		ArrayList<ReadSetEntry> readset = ((SetThread) Thread.currentThread()).list_readset;
	//	System.out.println("head.next"+head.next.getReference().value);
		Node pred = head, curr = head.getNext();
		//System.out.println("value of curr:"+curr.key);
		//System.out.println(makeRegularKey(item));
		while (curr.key < item)
		{
			pred = curr;
			curr = curr.next.getReference();
		}
		
		int currItem = curr.value;
		boolean currMarked = curr.marked;
		
		//System.out.println("curritem="+currItem+",item="+item);
		
		if(!postValidate(readset))
			throw AbortedException.abortedException;
		//System.out.println("postvalidation successful");
		if(curr.key == item && !currMarked)
		{
			
			if(type == CONTAINS)
			{
				readset.add(new ReadSetEntry(pred, curr, false));
				return true;
			}
			else if(type == ADD)
			{
				readset.add(new ReadSetEntry(pred, curr, false));
				//System.out.println("cant add");
				return false;
			}
			else // remove
			{
				readset.add(new ReadSetEntry(pred, curr, true));
				writeset.put(item, new WriteSetEntry(pred, curr, REMOVE, item));
				//System.out.println("can remove");
				return true;
			}
		}
		else
		{
			readset.add(new ReadSetEntry(pred, curr, true));
			if(type == CONTAINS || type == REMOVE)
				return false;
			else // add
			{
				writeset.put(item, new WriteSetEntry(pred, curr, ADD, item));
				return true;
			}
		}
	}
  
  public OptimisticBoostedBucketList getSentinel(int index) {
    int key = makeSentinelKey(index);
    boolean splice;
    while (true) {
      // find predecessor and current entries
      Window window = find(head, key);
      Node pred = window.pred;
      Node curr = window.curr;
      // is the key present?
      if (curr.key == key) {
        return new OptimisticBoostedBucketList(curr);
      } else {
        // splice in new entry
        Node entry = new Node(key);
        entry.next.set(pred.next.getReference(), false);
        splice = pred.next.compareAndSet(curr, entry, false, false);
        if (splice)
          return new OptimisticBoostedBucketList(entry);
        else
          continue;
      }
    }
  }
  public static int reverse(int key) {
    int loMask = LO_MASK;
    int hiMask = HI_MASK;
    int result = 0;
    for (int i = 0; i < WORD_SIZE; i++) {
      if ((key & loMask) != 0) {  // bit set
        result |= hiMask;
      }
      loMask <<= 1;
      hiMask >>>= 1;  // fill with 0 from left
    }
    return result;
  }
  public int makeRegularKey(int x) {
    int code = ((Integer)x).hashCode() & MASK; // take 3 lowest bytes
    //System.out.println("makesentinel-"+reverse(code | HI_MASK));
    return reverse(code | HI_MASK);
  }
  private int makeSentinelKey(int key) {
    return reverse(key & MASK);
  }
  // iterate over Set elements
  public Iterator iterator() {
    throw new UnsupportedOperationException();
  }
  private class Node {
    int key;
    int value;
    volatile boolean marked=false;
    volatile AtomicInteger lock = new AtomicInteger(0);
	volatile long lockHolder = -1;
    AtomicMarkableReference<Node> next;
    Node(int key, int object) {      // usual constructor
      this.key   = key;
      this.value = object;
      this.next  = new AtomicMarkableReference<Node>(null, false);
    }
    Node(int key) { // sentinel constructor
      this.key  = key;
      this.next = new AtomicMarkableReference<Node>(null, false);
    }
    
    Node getNext() {
      boolean[] cMarked = {false}; // is curr marked?
      boolean[] sMarked = {false}; // is succ marked?
      Node entry = this.next.get(cMarked);
      while (cMarked[0]) {
        Node succ = entry.next.get(sMarked);
        this.next.compareAndSet(entry, succ, true, sMarked[0]);
        entry = this.next.get(cMarked);
      }
      return entry;
    }
  }
  class Window {
    public Node pred;
    public Node curr;
    Window(Node pred, Node curr) {
      this.pred = pred;
      this.curr = curr;
    }
  }
  public Window find(Node head, int key) {
    Node pred = head;
    Node curr = head.next.getReference();
    while (curr.key < key) {
      pred = curr;
      curr = pred.next.getReference();
    }
    return new Window(pred, curr);
  }
@Override
public void begin() {
	// TODO Auto-generated method stub
	
}

private boolean commitValidate(ArrayList<ReadSetEntry> readset) {
	ReadSetEntry entry;	
	int size = readset.size();

	// check the values of the nodes 
	for(int i=0; i<size; i++)
	{
		entry = readset.get(i);
		if(entry.curr.marked)
			return false;
		//doubt
		if(entry.checkLink && (entry.pred.marked || entry.curr != entry.pred.next.getReference()))
			return false;
	}
	return true;
}
@Override
public void commit() throws AbortedException {
	
	SetThread t = ((SetThread) Thread.currentThread());
	
	Set<Entry<Integer, WriteSetEntry>> write_set = t.list_writeset.entrySet();
	ArrayList<ReadSetEntry> read_set = t.list_readset;
	
	// read-only transactions do nothing
	if(write_set.isEmpty())
	{
		read_set.clear();
		return;
	}
	
	long threadId = Thread.currentThread().getId();
	Iterator<Entry<Integer, WriteSetEntry>> iterator = write_set.iterator();
	WriteSetEntry entry;
	
	int predLock, currLock;
	Node newNodeOrVictim;
	
	// Acquire locks
	while(iterator.hasNext())
	{
		entry = iterator.next().getValue();
		
		predLock = entry.pred.lock.get();
		currLock = entry.curr.lock.get();			
		
		// check that pred lock is not acquired by another thread
		if((predLock & 1) == 1 && entry.pred.lockHolder != threadId)
			throw AbortedException.abortedException;
		// if operation is REMOVE, check that curr lock is not acquired by another thread
		if(entry.operation == REMOVE && (currLock & 1) == 1 && entry.curr.lockHolder != threadId)
			throw AbortedException.abortedException;
		
		// Try to acquire pred lock
		if(entry.pred.lockHolder == threadId || entry.pred.lock.compareAndSet(predLock, predLock + 1))
		{
			// if operation is REMOVE, try to acquire curr lock
			entry.pred.lockHolder = threadId;
			if(entry.operation == REMOVE)
			{
				if(entry.curr.lockHolder == threadId || entry.curr.lock.compareAndSet(currLock, currLock + 1))
					entry.curr.lockHolder = threadId;
				// in case of failure, unlock pred and abort.
				else
				{
					entry.pred.lockHolder = -1;
					entry.pred.lock.decrementAndGet();
					throw AbortedException.abortedException;
				}
			}
		}
		else
			throw AbortedException.abortedException;
	}
	
	// validate read-set
	if(!commitValidate(t.list_readset))
		throw AbortedException.abortedException;

	// Publish write-set
	iterator = write_set.iterator();
	while(iterator.hasNext())
	{
		entry = iterator.next().getValue();
		
		
		Node pred = entry.pred;
		Node curr = entry.pred.next.getReference();
		while(curr.key < entry.item)
		{
			pred = curr;
			curr = curr.next.getReference();
		}
		
		if(entry.operation == ADD)
		{
			newNodeOrVictim = new Node(entry.item);
			newNodeOrVictim.lock.set(1);
			newNodeOrVictim.lockHolder = threadId;
			entry.newNode = newNodeOrVictim;
			newNodeOrVictim.next.set(curr, false);
			pred.next.compareAndSet(curr, newNodeOrVictim, false, false);
		}
		else // remove
		{
			curr.marked = true;
			//pred.next.compareAndSet(expectedReference, newReference, expectedMark, newMark)
			pred.next.set(entry.curr.next.getReference(),false);
			//pred.next = entry.curr.next;
			
		}			
	}
	
	// unlock
	iterator = write_set.iterator();
	while(iterator.hasNext())
	{
		entry = iterator.next().getValue();
		if(entry.pred.lockHolder == threadId)
		{
			entry.pred.lockHolder = -1;
			entry.pred.lock.incrementAndGet();
		}
		// newNodeOrVictim in this case is either the added or the removed node 
		if(entry.operation == REMOVE)
			newNodeOrVictim = entry.curr;
		else // add
			newNodeOrVictim = entry.newNode;
		if (newNodeOrVictim.lockHolder == threadId) {
			newNodeOrVictim.lockHolder = -1;
			newNodeOrVictim.lock.incrementAndGet();
		}
	}
	
	// clear read- and write- sets
	read_set.clear();
	write_set.clear();
}
@Override
public void abort() {
	SetThread t = ((SetThread) Thread.currentThread());
	
	// internally you must not call this method. It's sufficient to throw AbortException
	// This method will be called from calling transaction as a part of its abort subroutine
	// This way: Abort is handled by transaction not by data structure.
	
	// Unlock
	Iterator<Entry<Integer, WriteSetEntry>> iterator = t.list_writeset.entrySet().iterator();
	WriteSetEntry entry;
	while(iterator.hasNext())
	{
		entry = iterator.next().getValue();
		if(entry.pred.lockHolder == t.getId())
		{
			entry.pred.lockHolder = -1;
			entry.pred.lock.decrementAndGet();
		}
			
		if(entry.operation == REMOVE && entry.curr.lockHolder == t.getId())
		{
			{
				entry.curr.lockHolder = -1;
				entry.curr.lock.decrementAndGet();
			}
		}
	}		
	
	// clear read- and write- sets
	t.list_readset.clear();
	t.list_writeset.clear();
	
}
@Override
public boolean nontransactionalAdd(int item)
{
	Node pred = head, curr = head.next.getReference();
	while (curr.key < makeRegularKey(item))
	{
		pred = curr;
		curr = curr.next.getReference();
	}
	//System.out.println("curr="+curr.key+"key of item to be added:="+makeRegularKey(item));
	if(curr.key == makeRegularKey(item))
		return false;
	else
	{
		Node node = new Node(makeRegularKey(item));
		node.next.set(curr, false); 
		pred.next.set(node, false);
		//System.out.println("non-transactionally added");
	//	System.out.println("head after add="+head.next.getReference().value);
		return true;
	}
	
}


}
