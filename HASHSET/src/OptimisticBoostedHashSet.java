





import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;




public class OptimisticBoostedHashSet implements IntSet {
	
	final int ADD = 0;
	final int REMOVE = 1;
	final int CONTAINS = 2;
	
	  protected OptimisticBoostedBucketList[] bucket;
	  protected AtomicInteger bucketSize;
	  protected AtomicInteger setSize;
	  private static final double THRESHOLD = 4.0;
	  
	  public OptimisticBoostedHashSet(int capacity) {
		    bucket = (OptimisticBoostedBucketList[]) new OptimisticBoostedBucketList[capacity];
		    bucket[0] = new OptimisticBoostedBucketList();
		    bucketSize = new AtomicInteger(2);
		    setSize = new AtomicInteger(0);
		  }
	  public boolean add(int x) throws AbortedException {
		    return operation(ADD, x);
	  }
	  
	  public boolean remove(int x) throws AbortedException {
			return operation(REMOVE, x);
		}
	  
	  public boolean contains(int x) throws AbortedException {
			return operation(CONTAINS, x);
		}
	  

	  
	  private boolean operation(int type, int item) throws AbortedException
		{
		 // System.out.println("bucketsize is"+bucketSize.get());
		  int myBucket = Math.abs(OptimisticBoostedBucketList.hashCode(item) % bucketSize.get());
		 // System.out.println("my bucket is"+myBucket);;
		  OptimisticBoostedBucketList b = getBucketList(myBucket);
		  ArrayList<OptimisticBoostedBucketList> HashWriteset = ((SetThread) Thread.currentThread()).list_HashmapWriteset;
		  if(!HashWriteset.contains(b))
		  HashWriteset.add(b);
		 // ((SetThread) Thread.currentThread()).list_HashmapWriteset=HashWriteset;
		  if(type==ADD)
		  {
		  if (!b.add(item))
		      return false;
		  ((SetThread) Thread.currentThread()).count++;
		  return true;
		  }
		  else if(type==REMOVE)
		  {
		  if(!b.remove(item)){
			  //System.out.println("cant remove");
			  return false;
		  }
		  //System.out.println("can be removed");
		  ((SetThread) Thread.currentThread()).count--;
		  return true;
		  }
		  else if(type==CONTAINS){
			  if(!b.contains(item))
				  return false;
		  return true;
		  }
		  return false;
		}
	  
	  private OptimisticBoostedBucketList getBucketList(int myBucket) {
		    if (bucket[myBucket] == null)
		      initializeBucket(myBucket);
		    return bucket[myBucket];
		  }
		  private void initializeBucket(int myBucket) {
		    int parent = getParent(myBucket);
		    if (bucket[parent] == null)
		      initializeBucket(parent);
		    OptimisticBoostedBucketList b = bucket[parent].getSentinel(myBucket);
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
		
		  public void commit() throws AbortedException {
			  SetThread t = ((SetThread) Thread.currentThread());
			  ArrayList<OptimisticBoostedBucketList> HashWriteset = t.list_HashmapWriteset;
			  for(OptimisticBoostedBucketList b:HashWriteset)
			  {
				  b.commit();
				  setSize.compareAndSet(setSize.get(), setSize.get()+t.count);
				  int setSizeNow=setSize.get()+t.count;
				  int bucketSizeNow = bucketSize.get();
				  if (setSizeNow / bucketSizeNow > THRESHOLD)
				  if(2 * bucketSizeNow<bucket.length)
				  bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
				  else
				  abort();
				  
			  }
			  HashWriteset.clear();
			  t.count=0;
		  }
		@Override
		public void begin() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void abort() {
			SetThread t = ((SetThread) Thread.currentThread());
			ArrayList<OptimisticBoostedBucketList> HashWriteset = t.list_HashmapWriteset;
			  for(OptimisticBoostedBucketList b:HashWriteset)
			  {b.abort();
			  
			  }
			
		}
		@Override
		public boolean nontransactionalAdd(int x) {
			int myBucket = Math.abs(BucketList.hashCode(x) % bucketSize.get());
			OptimisticBoostedBucketList b = getBucketList(myBucket);
		    if (!b.nontransactionalAdd(x))
		      return false;
		    int setSizeNow = setSize.getAndIncrement();
		    int bucketSizeNow = bucketSize.get();
		    if (setSizeNow / bucketSizeNow > THRESHOLD)
				  if(2 * bucketSizeNow<bucket.length)
				  bucketSize.compareAndSet(bucketSizeNow, 2 * bucketSizeNow);
		    return true;
		}
	  
	  
	  
	  
}
