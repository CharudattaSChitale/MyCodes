/*
 * LockFreeHashSet.java
 *
 * Created on December 30, 2005, 12:48 AM
 *
 * From "Multiprocessor Synchronization and Concurrent Data Structures",
 * by Maurice Herlihy and Nir Shavit.
 * Copyright 2006 Elsevier Inc. All rights reserved.
 */





import java.util.concurrent.atomic.AtomicInteger;

/**
 * @param T item type
 * @author Maurice Herlihy
 */
public class LockFreeHashSet implements IntSet {
  protected BucketList[] bucket;
  protected AtomicInteger bucketSize;
  protected AtomicInteger setSize;
  private static final double THRESHOLD = 4.0;
  /**
   * Constructor
   * @param capacity max number of bucket
   */
  public LockFreeHashSet(int capacity) {
    bucket = (BucketList[]) new BucketList[capacity];
    bucket[0] = new BucketList();
    bucketSize = new AtomicInteger(2);
    setSize = new AtomicInteger(0);
  }
  /**
   * Add item to set
   * @param x item to add
   * @return <code>true</code> iff set changed.
   */
  public boolean add(int x) {
    int myBucket = Math.abs(BucketList.hashCode(x) % bucketSize.get());
    BucketList b = getBucketList(myBucket);
    if (!b.add(x))
      return false;
    int setSizeNow = setSize.getAndIncrement();
    int bucketSizeNow = bucketSize.get();
    if (setSizeNow / bucketSizeNow > THRESHOLD)
		  if(2 * bucketSizeNow<bucket.length)
		  bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
    return true;
  }
  /**
   * Remove item from set
   * @param x item to remove
   * @return <code>true</code> iff set changed.
   */
  public boolean remove(int x) {
    int myBucket = Math.abs(BucketList.hashCode(x) % bucketSize.get());
    BucketList b = getBucketList(myBucket);
    if (!b.remove(x)) {
      return false;		// she's not there
    }
    return true;
  }
  public boolean contains(int x) {
    int myBucket = Math.abs(BucketList.hashCode(x) % bucketSize.get());
    BucketList b = getBucketList(myBucket);
    return b.contains(x);
  }
  private BucketList getBucketList(int myBucket) {
	  //System.out.println("Bucket:"+myBucket);
	  //System.out.println("BucketSize:"+bucketSize);
    if (bucket[myBucket] == null)
      initializeBucket(myBucket);
    return bucket[myBucket];
  }
  private void initializeBucket(int myBucket) {
    int parent = getParent(myBucket);
    if (bucket[parent] == null)
      initializeBucket(parent);
    BucketList b = bucket[parent].getSentinel(myBucket);
    if (b != null)
      bucket[myBucket] = b;
  }
  private int getParent(int myBucket){
    int parent = bucketSize.get();
    do {
      parent = parent >> 1;
    } while (parent > myBucket);
    parent = myBucket - parent;
    return parent;
  }
@Override
public void begin() {
	// TODO Auto-generated method stub
	
}
@Override
public void commit() throws AbortedException {
	// TODO Auto-generated method stub
	
}
@Override
public void abort() {
	// TODO Auto-generated method stub
	
}
@Override
public boolean nontransactionalAdd(int item) {
	// TODO Auto-generated method stub
	return false;
}
}



