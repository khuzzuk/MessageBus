package pl.khuzzuk.messaging.processor;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BusWorkerPool<T extends Enum<T>> {
   private BusWorker<T>[] quickCacheWorkers;
   private Queue<Runnable> quickCache;
   private BusWorker<T> slowCacheWorker;
   private Queue<Runnable> slowCache;

   @SuppressWarnings("unchecked")
   public static <T extends Enum<T>> BusWorkerPool<T> start(int threads) {
      BusWorker<T>[] workers = new BusWorker[threads];
      Queue<Runnable> quickCache = new ArrayBlockingQueue<>(threads * 8);
      for (int i = 0; i < threads; i++) {
         workers[i] = new BusWorker<>(quickCache);
         workers[i].start();
      }
      LinkedBlockingQueue<Runnable> slowCache = new LinkedBlockingQueue<>();
      BusWorker<T> slowCacheWorker = new BusWorker<>(slowCache);
      slowCacheWorker.start();

      BusWorkerPool<T> busWorkerPool = new BusWorkerPool<>();
      busWorkerPool.quickCacheWorkers = workers;
      busWorkerPool.quickCache = quickCache;
      busWorkerPool.slowCacheWorker = slowCacheWorker;
      busWorkerPool.slowCache = slowCache;
      return busWorkerPool;
   }

   void addOrWait(BusTask<T> busTask) {
      boolean succeeded = quickCache.offer(busTask);
      if (succeeded) {
         for (BusWorker<T> worker : quickCacheWorkers) {
            worker.wake();
         }
      } else if (Thread.currentThread() instanceof BusWorker) {
         slowCache.offer(busTask);
         slowCacheWorker.wake();
      } else {
         while (!succeeded) {
            succeeded = quickCache.offer(busTask);
         }
      }
   }

   void close() {
      for (BusWorker<T> worker : quickCacheWorkers) {
         worker.closed.set(true);
         worker.wake();
      }
   }
}
