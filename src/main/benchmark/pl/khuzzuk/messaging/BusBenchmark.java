package pl.khuzzuk.messaging;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BusBenchmark
{
   private Bus<MessageType> bus;
   private Set<MessageType> msgs = EnumSet.allOf(MessageType.class);
   private double setSize = msgs.size();
   private AtomicInteger counter = new AtomicInteger(0);
   private Runtime runtime;
   private NumberFormat format;

   public static void main(String[] args) throws InterruptedException {
      BusBenchmark benchmark = new BusBenchmark();
      benchmark.run();
      benchmark.run();
   }

   private void run() throws InterruptedException {
      init();
      System.out.println("Sending before warm up"); //NOSONAR
      simpleSend(100_000);
      tryCleaningMemory();
      warmUp();
      tryCleaningMemory();
      simpleSend(2_000_000);
      close();
   }

   private void init() {
      runtime = Runtime.getRuntime();
      format = NumberFormat.getNumberInstance();
      System.out.println(); //NOSONAR
      System.out.println("Initializing bus"); //NOSONAR
      bus = Bus.initializeBus(MessageType.class);
      for (MessageType mgs : msgs)
      {
         bus.subscribingFor(mgs).then(counter::incrementAndGet).subscribe();
      }
   }

   private void warmUp() {
      System.out.println(); //NOSONAR
      System.out.println("Starting warm up"); //NOSONAR
      reportMemory();
      for (long i = 0; i < 3_000_000; i++) {
         if (i % 1_000_000 == 0)  {
            System.out.println("initialization iterated for " + i); //NOSONAR
         }
         for (MessageType msg : msgs) {
            bus.message(msg).send();
         }
      }
      reportMemory();
   }

   private void close() {
      System.out.println("Closing the bus"); //NOSONAR
      bus.closeBus();
   }

   private void simpleSend(long reps) {
      reportMemory();
      System.out.println(); //NOSONAR
      System.out.println("Start sending messages - " + format.format(reps * setSize)); //NOSONAR
      long start = System.nanoTime();
      for (long i = 0; i < reps; i++)
      {
         for (MessageType msg : msgs)
         {
            bus.message(msg).send();
         }
      }
      long timeElapsed = (System.nanoTime() - start) / 1_000_000L;
      System.out.println("Finished sending messages, total time was " + timeElapsed + "ms"); //NOSONAR
      double rateNum = ((double) reps * setSize * 1000d) / ((double) timeElapsed);
      String rate = format.format(rateNum);
      System.out.println("Rate is " + rate + " messages per second"); //NOSONAR
      reportMemory();
   }

   private void reportMemory() {
      System.out.println(); //NOSONAR
      System.out.println("Memory usage:"); //NOSONAR
      System.out.println("Total memory: " + format.format(runtime.totalMemory())); //NOSONAR
      System.out.println("Memory in use: " + format.format(runtime.totalMemory() - runtime.freeMemory())); //NOSONAR
   }

   private void tryCleaningMemory() throws InterruptedException {
      System.gc(); //NOSONAR
      System.out.println(); //NOSONAR
      System.out.println("Try to clean memory for 2 seconds"); //NOSONAR
      Thread.sleep(2000);
      System.out.println("Memory cleaning done"); //NOSONAR
      System.out.println(); //NOSONAR
   }

   private enum MessageType
   {
      MSG1, MSG2, MSG3, MSG4, MSG5
   }
}
