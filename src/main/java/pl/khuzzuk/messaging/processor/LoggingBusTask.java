package pl.khuzzuk.messaging.processor;

public class LoggingBusTask<T extends Enum<T>> extends BusTask<T>
{
   public LoggingBusTask(BusContext<T> busContext) {
      super(busContext);
   }

   @Override
   public void run()
   {
      busContext.out.println(String.format("Receiving: %s%n\tfor: %s", message, subscriber));
      super.run();
      busContext.out.println(String.format("Finished: %s%n\tfor: %s", message, subscriber));
   }
}
