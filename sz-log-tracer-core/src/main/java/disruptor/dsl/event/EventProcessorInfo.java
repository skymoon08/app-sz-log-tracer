package disruptor.dsl.event;


import disruptor.dsl.ConsumerInfo;
import disruptor.event.EventProcessor;
import disruptor.handler.EventHandler;
import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

import java.util.concurrent.Executor;

/**
 * <p>Wrapper class to tie together a particular event processing stage</p>
 * <p>
 * <p>Tracks the event processor instance, the event handler instance, and sequence barrier which the stage is attached to.</p>
 *
 * @param <T> the type of the configured {@link EventHandler}
 */
public class EventProcessorInfo<T> implements ConsumerInfo {
    private final EventProcessor eventprocessor;
    private final EventHandler<? super T> handler;
    private final SequenceBarrier barrier;
    private boolean                       endOfChain = true;

    public EventProcessorInfo(final EventProcessor eventprocessor, final EventHandler<? super T> handler,
                              final SequenceBarrier barrier) {
        this.eventprocessor = eventprocessor;
        this.handler = handler;
        this.barrier = barrier;
    }

    public EventProcessor getEventProcessor() {
        return eventprocessor;
    }

    @Override
    public Sequence[] getSequences() {
        return new Sequence[] { eventprocessor.getSequence() };
    }

    public EventHandler<? super T> getHandler() {
        return handler;
    }

    @Override
    public SequenceBarrier getBarrier() {
        return barrier;
    }

    @Override
    public boolean isEndOfChain() {
        return endOfChain;
    }

    @Override
    public void start(final Executor executor) {
        executor.execute(eventprocessor);
    }

    @Override
    public void halt() {
        eventprocessor.halt();
    }

    /**
     *
     */
    @Override
    public void markAsUsedInBarrier() {
        endOfChain = false;
    }

    @Override
    public boolean isRunning() {
        return eventprocessor.isRunning();
    }
}
