package disruptor.exception;

/**
 * Callback handler for uncaught exceptions in the event processing cycle of the {@link disruptor.sequence.BatchEventProcessor}
 */
public interface ExceptionHandler<T> {
    /**
     * <p>Strategy for handling uncaught exceptions when processing an event.</p>
     *
     * <p>If the strategy wishes to terminate further processing by the {@link disruptor.sequence.BatchEventProcessor}
     * then it should throw a {@link RuntimeException}.</p>
     *
     * @param ex       the exception that propagated from the {@link java.beans.EventHandler}.
     * @param sequence of the event which cause the exception.
     * @param event    being processed when the exception occurred.  This can be null.
     */
    void handleEventException(Throwable ex, long sequence, T event);

    /**
     * Callback to notify of an exception during {@link disruptor.strategy.LifecycleAware#onStart()}
     *
     * @param ex throw during the starting process.
     */
    void handleOnStartException(Throwable ex);

    /**
     * Callback to notify of an exception during {@link disruptor.strategy.LifecycleAware#onShutdown()}
     *
     * @param ex throw during the shutdown process.
     */
    void handleOnShutdownException(Throwable ex);
}
