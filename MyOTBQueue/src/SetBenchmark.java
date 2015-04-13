import java.util.Random;




public class SetBenchmark implements Benchmark {
	myQueueSet m_set;
	int m_range = 1 << 16;
	int m_rate = 50;
	int m_ops = 1;
	int nb_threads = 1;
	final static int MAX_OPERATIONS_PER_TRANSACTION = 10;

	public void init(String[] args, int nb_threads) {
		boolean error = false;
		int initial = 256;
		this.nb_threads = nb_threads;
		if (args.length > 0) {
			if (args[0].equals("OptimisticBoostedQueue"))
				m_set = new OptimisticBoostedQueue();
			else if(args[0].equals("UnboundedQueue"))
				m_set = new UnboundedQueue();
			else if(args[0].equals("STMQueue"))
				m_set = new STMQueue();
			else
				error = true;
		} else
			error = true;
		for (int i = 1; i < args.length && !error; i++) {
			if (args[i].equals("-i")) {
				if (++i < args.length)
					initial = Integer.parseInt(args[i]);
				else
					error = true;
			} else if (args[i].equals("-r")) {
				if (++i < args.length)
					m_range = Integer.parseInt(args[i]);
				else
					error = true;
			} else if (args[i].equals("-w")) {
				if (++i < args.length)
					m_rate = Integer.parseInt(args[i]);
				else
					error = true;
			} else if (args[i].equals("-o")) {
				if (++i < args.length)
					m_ops = Integer.parseInt(args[i]);
				else
					error = true;
			} else
				error = true;
		}
		if (error) {
			System.out
					.println("Benchmark arguments: (LockFree|Lazy|Boosted|OptimisticBoosted) [-o ops-per-transaction] [-i initial-size] [-r range] [-w write-rate]");
			System.exit(1);
		}
		Random random = new Random();
		int c = 0;
		// exclude items which will be inserted in the beginning of each
		// transaction to create a local history of added items
		// assuming that they will be all added (which is true if range is
		// fairly large)
		// NOTE: If initial is too small so there may be an error in
		// calculations because the minimum to be added is
		// nb_threads*MAX_OPERATIONS_PER_TRANSACTION/2
		int deferred_adds = nb_threads * MAX_OPERATIONS_PER_TRANSACTION / 2;
		for (int i = 0; i < initial - deferred_adds; i++) {
			;
			if (m_set.nontransactionalEnq(random.nextInt(m_range)))
				c++;
		}
		System.out.println("Initial size        = "
				+ (c + (nb_threads * MAX_OPERATIONS_PER_TRANSACTION / 2)));
		System.out.println("Range               = " + m_range);
		System.out.println("Write rate          = " + m_rate + "%");
		System.out.println("Ops per transaction	= " + m_ops);
		System.out.println();
	}

	public BenchmarkThread createThread(int i, int nb) {
		return new SetThread(m_set, m_range, m_rate, m_ops);
	}

	public String getStats(BenchmarkThread[] threads) {
		int enq = 0;
		int deq = 0;
		int succ_enq = 0;
		int succ_deq = 0;
		//int contains = 0;
		int aborts = 0;
		for (int i = 0; i < threads.length; i++) {
			enq += ((SetThread) threads[i]).m_nb_enq;
			deq += ((SetThread) threads[i]).m_nb_deq;
			succ_enq += ((SetThread) threads[i]).m_nb_succ_enq;
			succ_deq += ((SetThread) threads[i]).m_nb_succ_deq;
			//contains += ((SetThread) threads[i]).m_nb_contains;
			aborts += ((SetThread) threads[i]).m_nb_aborts;
		}
		return "E=" + enq + ", D=" + deq + ", SE=" + succ_enq + ", SD="
				+ succ_deq /*+ ", C=" + contains */+ ", Aborts=" + aborts;
	}
}
