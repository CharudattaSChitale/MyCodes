

Command line arguments:

For HASHSET project 
-n 10 -d 6000 -w 2000 SetBenchmark IntSetHash -o 10

-n 10 -d 6000 -w 2000 SetBenchmark LockFreeHashSet -o 10

-n 10 -d 6000 -w 2000 SetBenchmark OptimisticBoostedHashSet -o 10



For MyOTBQueue project
-n 10 -d 6000 -w 2000 SetBenchmark STMQueue -o 10

-n 10 -d 6000 -w 2000 SetBenchmark OptimisticBoostedQueue -o 10

-n 10 -d 6000 -w 2000 SetBenchmark UnboundedQueue -o 10




Note: The output starts with a series of prin statements before giving the final results.