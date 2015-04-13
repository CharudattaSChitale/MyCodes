import java.util.ArrayList;
import java.util.Random;




public class SetThread extends BenchmarkThread {
	final private myQueueSet m_set;
	final private int m_range;
	final private int m_rate;
	final private int m_ops;
	int m_nb_enq;
	int m_nb_deq;
	int m_nb_succ_enq;
	int m_nb_succ_deq;
	int m_nb_contains;
	int m_nb_aborts;
	
	
	int[] operationType = new int[SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION];
	int[] enqItems = new int[SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION];
	
	int[] m_last = new int[SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION];
	int[] oldm_last = new int[SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION];
	int add_index = SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION/2, remove_index = 0;
	int old_add_index, old_remove_index;
	boolean m_write;
	
	final private Random m_random;
	
	int localVersion = 0;

	

	// //////////////////////////////////////////////////////////////
	// for optimistic boosting
	
	QueueLinkedList writeQueueThread=new QueueLinkedList();
	Node enqReadSet=new Node();
	Node deqReadSet=new Node();
	ArrayList list =new ArrayList();
	
	Node localHead=new Node();
	
	public SetThread(myQueueSet set, int range, int rate, int ops) {
		m_set = set;
		m_range = range;
		m_ops = ops;
		m_nb_enq = m_nb_deq = 0;
		m_rate = rate;
		//m_write = true;
		m_random = new Random();
	}

	protected void step(int phase) {
		
		for(int c = 0; c < m_ops; c++)
		{
			int i = m_random.nextInt(100);
			if (i < m_rate)
				operationType[c] = 1; // enq
			else
				operationType[c] = 0; // deq
			
			//removeItems[c] = addItems[c];
			enqItems[c] = m_random.nextInt(100);
			
		}
		
		boolean flag = true, oldm_write = m_write;
		
		while (flag) {
			flag = false;
			//oldm_write = m_write;
			//old_add_index = add_index; 
			//old_remove_index = remove_index;
			//for(int i=0;i<SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION;i++)
				//oldm_last[i] = m_last[i];
			try {
				int nb_enq = 0, nb_deq = 0, nb_succ_enq = 0, nb_succ_deq = 0;
				//System.out.println("localHead:"+localHead+"deqreadset:"+deqReadSet);
				//System.out.println(m_set.getClass().toString());
				if(m_set.getClass().toString().equals("class OptimisticBoostedQueue"))
				{
				//System.out.println("not here");
				deqReadSet=m_set.getHead();
				enqReadSet=m_set.getTail();
				//localHead.next=deqReadSet.next;
				localHead=deqReadSet;
				//System.out.println(localHead+""+deqReadSet);
				}
				list.clear();
				for (int c = 0; c < m_ops; c++) {
					//System.out.println("transaction started");
					if (operationType[c] == 1) {
						//if (m_write) {
							if(m_set.enq(enqItems[c]))
							{
								//System.out.println("enqued successfully");
//								if(initial_adds == MAX_OPERATIONS_PER_TRANSACTION)
									//m_write = false;
//								else
//									initial_adds++;
								//m_last[add_index] = enqItems[c];
								//add_index++;
								//if(add_index == SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION)
								//	add_index = 0;
								if(phase == Benchmark.TEST_PHASE)
									nb_succ_enq++;
							}
							if (phase == Benchmark.TEST_PHASE)
								nb_enq++;
						/*} else {
							if(m_set.deq())
							{
								if (phase == Benchmark.TEST_PHASE)
									nb_succ_remove++;
							}
							remove_index++;
							if(remove_index == SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION)
								remove_index = 0;
							m_write = true;
							if (phase == Benchmark.TEST_PHASE)
								nb_remove++;
						}*/
					} else {
						if(m_set.deq())
						{if (phase == Benchmark.TEST_PHASE)
							//System.out.println("dequed successfully");
							nb_succ_deq++;
						}
						
						if (phase == Benchmark.TEST_PHASE)
							nb_deq++;
						}
						
						
						
					
				}
				//System.out.println("going to commit");
				m_set.commit();
				//System.out.println("commit successful");
				m_nb_enq += nb_enq;
				m_nb_deq += nb_deq;
				
				m_nb_succ_enq += nb_succ_enq;
				m_nb_succ_deq += nb_succ_deq;
				//System.out.println("exiting step function");
//				System.out.println("COMMIT: oldaddindex=" + old_add_index + " addindex=" + add_index + " oldremoveindex=" + old_remove_index + " removeindex=" + remove_index);
				
			} catch (AbortedException e) {
				flag = true;
				//for(int i=0; i<SetBenchmark.MAX_OPERATIONS_PER_TRANSACTION;i++)
				//	m_last[i] = oldm_last[i];
//				System.out.println("ABORT: oldaddindex=" + old_add_index + " addindex=" + add_index + " oldremoveindex=" + old_remove_index + " removeindex=" + remove_index);
				//add_index = old_add_index;
				//remove_index = old_remove_index;
				//m_write = oldm_write;
				m_nb_aborts++;
				m_set.abort();
				
				//
			}
		}
	}

	public String getStats() {
		return "E=" + m_nb_enq + ", D=" + m_nb_deq + ", SE=" + m_nb_succ_enq + ", SD=" + m_nb_succ_deq +
				", Aborts=" + m_nb_aborts;
	}
}
