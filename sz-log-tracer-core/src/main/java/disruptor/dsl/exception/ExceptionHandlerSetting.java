package disruptor.dsl.exception;


import disruptor.dsl.ConsumerRepository;
import disruptor.exception.ExceptionHandler;
import disruptor.handler.EventHandler;
import disruptor.sequence.BatchEventProcessor;

/**
 * A support class used as part of setting an exception handler for a specific event handler.
 * For example:
 * <pre><code>disruptorWizard.handleExceptionsIn(eventHandler).with(exceptionHandler);</code></pre>
 *
 * @param <T> the type of event being handled.
 */
public class ExceptionHandlerSetting<T> {
    private final EventHandler<T> eventHandler;
    private final ConsumerRepository<T> consumerRepository;

    public ExceptionHandlerSetting(final EventHandler<T> eventHandler,
                                   final ConsumerRepository<T> consumerRepository) {
        this.eventHandler = eventHandler;
        this.consumerRepository = consumerRepository;
    }

    /**
     * Specify the {@link ExceptionHandler} to use with the event handler.
     *
     * @param exceptionHandler the exception handler to use.
     */
    public void with(ExceptionHandler<? super T> exceptionHandler) {
        ((BatchEventProcessor<T>) consumerRepository.getEventProcessorFor(eventHandler))
                .setExceptionHandler(exceptionHandler);
        consumerRepository.getBarrierFor(eventHandler).alert();
    }
}
