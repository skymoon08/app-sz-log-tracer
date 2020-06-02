package disruptor.handler;

import com.sun.corba.se.impl.presentation.rmi.ExceptionHandler;

/**
 * Callback interface to be implemented for processing events as they become available in the {@link disruptor.buffer.RingBuffer}
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 * @see disruptor.sequence.BatchEventProcessor#setExceptionHandler(ExceptionHandler<? super T>) (ExceptionHandler) if you want to handle exceptions propagated out of the handler.
 */
public interface EventHandler<T> {
    /**
     * Called when a publisher has published an event to the {@link disruptor.buffer.RingBuffer}
     *
     * @param event      published to the {@link disruptor.buffer.RingBuffer}
     * @param sequence   of the event being processed
     * @param endOfBatch flag to indicate if this is the last event in a batch from the {@link disruptor.buffer.RingBuffer}
     * @throws Exception if the EventHandler would like the exception handled further up the chain.
     */
    void onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
}
