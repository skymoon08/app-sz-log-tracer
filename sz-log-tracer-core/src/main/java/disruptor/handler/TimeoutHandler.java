package disruptor.handler;

public interface TimeoutHandler {
    void onTimeout(long sequence) throws Exception;
}
