package pl.khuzzuk.messaging.processor;

import java.io.PrintStream;

public class BusContext<T extends Enum<T>>
{
   PrintStream out;
   final EventProcessor<T> eventProcessor;

   public BusContext(PrintStream out, EventProcessor<T> eventProcessor)
   {
      this.out = out;
      this.eventProcessor = eventProcessor;
   }

   public void setOut(PrintStream out)
   {
      this.out = out;
   }
}
