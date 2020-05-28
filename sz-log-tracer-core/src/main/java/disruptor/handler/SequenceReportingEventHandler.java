package disruptor.handler;


import disruptor.sequence.padding.Sequence;

/**
 * Used by the {@link disruptor.sequence.BatchEventProcessor} to set a callback allowing the {@link EventHandler} to notify
 * when it has finished consuming an event if this happens after the {@link EventHandler#onEvent(Object, long, boolean)} call.
 * <p>
 * Typically this would be used when the handler is performing some sort of batching operation such as writing to an IO
 * device; after the operation has completed, the implementation should call {@link Sequence#set} to update the
 * sequence and allow other processes that are dependent on this handler to progress.
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 */
public interface SequenceReportingEventHandler<T> extends EventHandler<T> {
    /**
     * Call by the {@link disruptor.sequence.BatchEventProcessor} to setup the callback.
     *
     * @param sequenceCallback callback on which to notify the {@link disruptor.sequence.BatchEventProcessor} that the sequence has progressed.
     */
    void setSequenceCallback(final Sequence sequenceCallback);
}
