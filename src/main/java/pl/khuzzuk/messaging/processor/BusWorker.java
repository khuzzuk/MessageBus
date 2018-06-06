package pl.khuzzuk.messaging.processor;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

class BusWorker<T extends Enum<T>> extends Thread
{
   final AtomicBoolean closed = new AtomicBoolean(false);
   private final Queue<Runnable> tasksCache;

   public BusWorker(Queue<Runnable> tasksCache) {
      this.tasksCache = tasksCache;
   }

   @Override
   public void run()
   {
      while (true) {
         if (tasksCache.size() == 0) LockSupport.park(this);
         Runnable task = tasksCache.poll();
         if (task != null) task.run();
         if (closed.get()) break;
      }
   }

   void wake() {
      LockSupport.unpark(this);
   }
}
