package pl.khuzzuk.messaging;

import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicInteger;


public class BusBenchmark
{
   private Bus bus;
   private String[] msgs = {"msg1", "msg2", "msg3", "msg4", "msg5"};
   private AtomicInteger counter = new AtomicInteger(0);
   private Runtime runtime;
   private NumberFormat format;

   public static void main(String[] args) {
      BusBenchmark benchmark = new BusBenchmark();
      benchmark.run();
      benchmark.run();
   }

   private void run() {
      init();
      System.out.println("Sending before warm up");
      simpleSend(100_000);
      warmUp();
      simpleSend(2_000_000);
      close();
   }

   private void init() {
      runtime = Runtime.getRuntime();
      format = NumberFormat.getNumberInstance();
      System.out.println();
      System.out.println("Initializing bus");
      bus = Bus.initializeBus(false);
      for (String mgs : msgs)
      {
         //bus.setReaction(mgs, counter::incrementAndGet);
      }
   }

   private void warmUp() {
      System.out.println();
      System.out.println("Starting warm up");
      for (long i = 0; i < 3_000_000; i++) {
         if (i % 1_000_000 == 0)  {
            System.out.println("initialization iterated for " + i);
         }
         for (String msg : msgs) {
            //bus.send(msg);
         }
      }
   }

   private void close() {
      System.out.println("Closing the bus");
      bus.closeBus();
   }

   private void simpleSend(long reps) {
      reportMemory();
      System.out.println();
      System.out.println("Start sending messages - " + format.format(reps * 5d));
      long start = System.nanoTime();
      for (long i = 0; i < reps; i++)
      {
         for (String msg : msgs)
         {
            //bus.send(msg);
         }
      }
      long timeElapsed = (System.nanoTime() - start) / 1_000_000L;
      System.out.println("Finished sending messages, total time was " + timeElapsed + "ms");
      double rateNum = ((double) reps * 5d * 1000d) / ((double) timeElapsed);
      String rate = format.format(rateNum);
      System.out.println("Rate is " + rate + " messages per second");
      reportMemory();
   }

   private void reportMemory() {
      System.out.println();
      System.out.println("Memory usage:");
      System.out.println("Total memory: " + format.format(runtime.totalMemory()));
      System.out.println("Memory in use: " + format.format(runtime.totalMemory() - runtime.freeMemory()));
   }
}
