package disruptor.dsl;

import disruptor.dsl.event.EventProcessorInfo;
import disruptor.dsl.worker.WorkerPool;
import disruptor.dsl.worker.WorkerPoolInfo;
import disruptor.event.EventProcessor;
import disruptor.handler.EventHandler;
import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a repository mechanism to associate {@link EventHandler}s with {@link disruptor.event.EventProcessor}s
 *
 * @param <T> the type of the {@link EventHandler}
 */
public class ConsumerRepository<T> implements Iterable<ConsumerInfo> {
    private final Map<EventHandler<?>, EventProcessorInfo<T>> eventProcessorInfoByEventHandler = new IdentityHashMap<EventHandler<?>, EventProcessorInfo<T>>();
    private final Map<Sequence, ConsumerInfo> eventProcessorInfoBySequence = new IdentityHashMap<Sequence, ConsumerInfo>();
    private final Collection<ConsumerInfo> consumerInfos = new ArrayList<ConsumerInfo>();

    public void add(final EventProcessor eventprocessor, final EventHandler<? super T> handler,
                    final SequenceBarrier barrier) {
        final EventProcessorInfo<T> consumerInfo = new EventProcessorInfo<T>(eventprocessor, handler, barrier);

        eventProcessorInfoByEventHandler.put(handler, consumerInfo);
        eventProcessorInfoBySequence.put(eventprocessor.getSequence(), consumerInfo);
        consumerInfos.add((ConsumerInfo) consumerInfo);
    }

    public void add(final EventProcessor processor) {
        final EventProcessorInfo<T> consumerInfo = new EventProcessorInfo<T>(processor, null, null);
        eventProcessorInfoBySequence.put(processor.getSequence(), consumerInfo);
        consumerInfos.add(consumerInfo);
    }

    public void add(final WorkerPool<T> workerPool, final SequenceBarrier sequenceBarrier) {
        final WorkerPoolInfo<T> workerPoolInfo = new WorkerPoolInfo<T>(workerPool, sequenceBarrier);
        consumerInfos.add(workerPoolInfo);
        for (Sequence sequence : workerPool.getWorkerSequences()) {
            eventProcessorInfoBySequence.put(sequence, workerPoolInfo);
        }
    }

    public Sequence[] getLastSequenceInChain(boolean includeStopped) {
        List<Sequence> lastSequence = new ArrayList<Sequence>();
        for (ConsumerInfo consumerInfo : consumerInfos) {
            if ((includeStopped || consumerInfo.isRunning()) && consumerInfo.isEndOfChain()) {
                final Sequence[] sequences = consumerInfo.getSequences();
                Collections.addAll(lastSequence, sequences);
            }
        }

        return lastSequence.toArray(new Sequence[lastSequence.size()]);
    }

    public EventProcessor getEventProcessorFor(final EventHandler<T> handler) {
        final EventProcessorInfo<T> eventProcessorInfo = getEventProcessorInfo(handler);
        if (eventProcessorInfo == null) {
            throw new IllegalArgumentException("The event handler " + handler + " is not processing events.");
        }

        return eventProcessorInfo.getEventProcessor();
    }

    public Sequence getSequenceFor(final EventHandler<T> handler) {
        return getEventProcessorFor(handler).getSequence();
    }

    public void unMarkEventProcessorsAsEndOfChain(final Sequence... barrierEventProcessors) {
        for (Sequence barrierEventProcessor : barrierEventProcessors) {
            getEventProcessorInfo(barrierEventProcessor).markAsUsedInBarrier();
        }
    }

    @Override
    public Iterator<ConsumerInfo> iterator() {
        return consumerInfos.iterator();
    }

    public SequenceBarrier getBarrierFor(final EventHandler<T> handler) {
        final ConsumerInfo consumerInfo = getEventProcessorInfo(handler);
        return consumerInfo != null ? consumerInfo.getBarrier() : null;
    }

    private EventProcessorInfo<T> getEventProcessorInfo(final EventHandler<T> handler) {
        return eventProcessorInfoByEventHandler.get(handler);
    }

    private ConsumerInfo getEventProcessorInfo(final Sequence barrierEventProcessor) {
        return eventProcessorInfoBySequence.get(barrierEventProcessor);
    }
}
