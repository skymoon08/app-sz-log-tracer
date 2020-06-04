package disruptor.sequence;

import disruptor.base.DataProvider;
import disruptor.event.EventProcessor;
import disruptor.exception.AlertException;
import disruptor.exception.ExceptionHandler;
import disruptor.exception.FatalExceptionHandler;
import disruptor.handler.EventHandler;
import disruptor.handler.SequenceReportingEventHandler;
import disruptor.handler.TimeoutHandler;
import disruptor.sequence.padding.Sequence;
import disruptor.strategy.LifecycleAware;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Convenience class for handling the batching semantics of consuming entries from a {@link disruptor.buffer.RingBuffer}
 * and delegating the available events to an {@link disruptor.handler.EventHandler}.
 * <p>
 * If the {@link disruptor.handler.EventHandler} also implements {@link disruptor.strategy.LifecycleAware} it will be notified just after the thread
 * is started and just before the thread is shutdown.
 *
 * @param <T> event implementation storing the data for sharing during exchange or parallel coordination of an event.
 */
public final class BatchEventProcessor<T> implements EventProcessor {
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ExceptionHandler<? super T> exceptionHandler = new FatalExceptionHandler();

    private final DataProvider<T> dataProvider;
    private final SequenceBarrier sequenceBarrier;
    private final EventHandler<? super T> eventHandler;
    private final Sequence sequence = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    private final TimeoutHandler timeoutHandler;

    /**
     * Construct a {@link EventProcessor} that will automatically track the progress by updating its sequence when
     * the {@link EventHandler#onEvent(Object, long, boolean)} method returns.
     *
     * @param dataProvider    to which events are published.
     * @param sequenceBarrier on which it is waiting.
     * @param eventHandler    is the delegate to which events are dispatched.
     */
    public BatchEventProcessor(final DataProvider<T> dataProvider,
                               final SequenceBarrier sequenceBarrier,
                               final EventHandler<? super T> eventHandler) {
        this.dataProvider = dataProvider;
        this.sequenceBarrier = sequenceBarrier;
        this.eventHandler = eventHandler;

        if (eventHandler instanceof SequenceReportingEventHandler) {
            ((SequenceReportingEventHandler<?>) eventHandler).setSequenceCallback(sequence);
        }

        timeoutHandler = (eventHandler instanceof TimeoutHandler) ? (TimeoutHandler) eventHandler : null;
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void halt() {
        running.set(false);
        sequenceBarrier.alert();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Set a new {@link ExceptionHandler} for handling exceptions propagated out of the {@link BatchEventProcessor}
     *
     * @param exceptionHandler to replace the existing exceptionHandler.
     */
    public void setExceptionHandler(final ExceptionHandler<? super T> exceptionHandler) {
        if (null == exceptionHandler) {
            throw new NullPointerException();
        }

        this.exceptionHandler = exceptionHandler;
    }

    /**
     * It is ok to have another thread rerun this method after a halt().
     *
     * @throws IllegalStateException if this object instance is already running in a thread
     */
    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Thread is already running");
        }
        sequenceBarrier.clearAlert();

        notifyStart();

        T event = null;
        long nextSequence = sequence.get() + 1L;
        try {
            while (true) {
                try {
                    final long availableSequence = sequenceBarrier.waitFor(nextSequence);

                    while (nextSequence <= availableSequence) {
                        event = dataProvider.get(nextSequence);
                        eventHandler.onEvent(event, nextSequence, nextSequence == availableSequence);
                        nextSequence++;
                    }
                    
                    sequence.set(availableSequence);
                } catch (final TimeoutException e) {
                    notifyTimeout(sequence.get());
                } catch (final AlertException ex) {
                    if (!running.get()) {
                        break;
                    }
                } catch (final Throwable ex) {
                    exceptionHandler.handleEventException(ex, nextSequence, event);
                    sequence.set(nextSequence);
                    nextSequence++;
                }
            }
        } finally {
            notifyShutdown();
            running.set(false);
        }
    }

    private void notifyTimeout(final long availableSequence) {
        try {
            if (timeoutHandler != null) {
                timeoutHandler.onTimeout(availableSequence);
            }
        } catch (Throwable e) {
            exceptionHandler.handleEventException(e, availableSequence, null);
        }
    }

    /**
     * Notifies the EventHandler when this processor is starting up
     */
    private void notifyStart() {
        if (eventHandler instanceof LifecycleAware) {
            try {
                ((LifecycleAware) eventHandler).onStart();
            } catch (final Throwable ex) {
                exceptionHandler.handleOnStartException(ex);
            }
        }
    }

    /**
     * Notifies the EventHandler immediately prior to this processor shutting down
     */
    private void notifyShutdown() {
        if (eventHandler instanceof LifecycleAware) {
            try {
                ((LifecycleAware) eventHandler).onShutdown();
            } catch (final Throwable ex) {
                exceptionHandler.handleOnShutdownException(ex);
            }
        }
    }
}