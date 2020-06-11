package disruptor.dsl.event;

import disruptor.dsl.ConsumerRepository;
import disruptor.dsl.Disruptor;
import disruptor.dsl.worker.WorkHandler;
import disruptor.event.EventProcessor;
import disruptor.handler.EventHandler;
import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

import java.util.Arrays;

/**
 * Copyright (C), 上海维跃信息科技有限公司
 * FileName: EventHandlerGroup
 * Author:   zhanglong
 * Date:     2020/5/27 20:15
 * Description:
 */
public class EventHandlerGroup<T> {
    private final Disruptor<T> disruptor;
    private final ConsumerRepository<T> consumerRepository;
    private final Sequence[] sequences;

    public EventHandlerGroup(final Disruptor<T> disruptor, final ConsumerRepository<T> consumerRepository,
                             final Sequence[] sequences) {
        this.disruptor = disruptor;
        this.consumerRepository = consumerRepository;
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    /**
     * Create a new event handler group that combines the consumers in this group with <tt>otherHandlerGroup</tt>.
     *
     * @param otherHandlerGroup the event handler group to combine.
     * @return a new EventHandlerGroup combining the existing and new consumers into a single dependency group.
     */
    public EventHandlerGroup<T> and(final EventHandlerGroup<T> otherHandlerGroup) {
        final Sequence[] combinedSequences = new Sequence[this.sequences.length
                + otherHandlerGroup.sequences.length];
        System.arraycopy(this.sequences, 0, combinedSequences, 0, this.sequences.length);
        System.arraycopy(otherHandlerGroup.sequences, 0, combinedSequences, this.sequences.length,
                otherHandlerGroup.sequences.length);
        return new EventHandlerGroup<T>(disruptor, consumerRepository, combinedSequences);
    }

    /**
     * Create a new event handler group that combines the handlers in this group with <tt>processors</tt>.
     *
     * @param processors the processors to combine.
     * @return a new EventHandlerGroup combining the existing and new processors into a single dependency group.
     */
    public EventHandlerGroup<T> and(final EventProcessor... processors) {
        Sequence[] combinedSequences = new Sequence[sequences.length + processors.length];

        for (int i = 0; i < processors.length; i++) {
            consumerRepository.add(processors[i]);
            combinedSequences[i] = processors[i].getSequence();
        }
        System.arraycopy(sequences, 0, combinedSequences, processors.length, sequences.length);

        return new EventHandlerGroup<T>(disruptor, consumerRepository, combinedSequences);
    }

    /**
     * Set up batch handlers to consume events from the ring buffer. These handlers will only process events
     * after every {@link EventProcessor} in this group has processed the event.
     *
     * <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     * process events before handler <code>B</code>:</p>
     *
     * <pre><code>dw.handleEventsWith(A).then(B);</code></pre>
     *
     * @param handlers the batch handlers that will process events.
     * @return a {@link EventHandlerGroup} that can be used to set up a event processor barrier over the created event processors.
     */
    public EventHandlerGroup<T> then(final EventHandler<? super T>... handlers) {
        return handleEventsWith(handlers);
    }

    /**
     * <p>Set up custom event processors to handle events from the ring buffer. The Disruptor will
     * automatically start these processors when {@link Disruptor#start()} is called.</p>
     *
     * <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     * process events before handler <code>B</code>:</p>
     *
     * @param eventProcessorFactories the event processor factories to use to create the event processors that will process events.
     * @return a {@link EventHandlerGroup} that can be used to chain dependencies.
     */
    public EventHandlerGroup<T> then(final EventProcessorFactory<T>... eventProcessorFactories) {
        return handleEventsWith(eventProcessorFactories);
    }

    /**
     * Set up a worker pool to handle events from the ring buffer. The worker pool will only process events
     * after every {@link EventProcessor} in this group has processed the event. Each event will be processed
     * by one of the work handler instances.
     *
     * <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     * process events before the worker pool with handlers <code>B, C</code>:</p>
     *
     * <pre><code>dw.handleEventsWith(A).thenHandleEventsWithWorkerPool(B, C);</code></pre>
     *
     * @param handlers the work handlers that will process events. Each work handler instance will provide an extra thread in the worker pool.
     * @return a {@link EventHandlerGroup} that can be used to set up a event processor barrier over the created event processors.
     */
    public EventHandlerGroup<T> thenHandleEventsWithWorkerPool(final WorkHandler<? super T>... handlers) {
        return handleEventsWithWorkerPool(handlers);
    }

    /**
     * Set up batch handlers to handle events from the ring buffer. These handlers will only process events
     * after every {@link EventProcessor} in this group has processed the event.
     *
     * <p>This method is generally used as part of a chain. For example if <code>A</code> must
     * process events before <code>B</code>:</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param handlers the batch handlers that will process events.
     * @return a {@link EventHandlerGroup} that can be used to set up a event processor barrier over the created event processors.
     */
    public EventHandlerGroup<T> handleEventsWith(final EventHandler<? super T>... handlers) {
        return disruptor.createEventProcessors(sequences, handlers);
    }

    /**
     * <p>Set up custom event processors to handle events from the ring buffer. The Disruptor will
     * automatically start these processors when {@link Disruptor#start()} is called.</p>
     *
     * <p>This method is generally used as part of a chain. For example if <code>A</code> must
     * process events before <code>B</code>:</p>
     *
     * <pre><code>dw.after(A).handleEventsWith(B);</code></pre>
     *
     * @param eventProcessorFactories the event processor factories to use to create the event processors that will process events.
     * @return a {@link EventHandlerGroup} that can be used to chain dependencies.
     */
    public EventHandlerGroup<T> handleEventsWith(final EventProcessorFactory<T>... eventProcessorFactories) {
        return disruptor.createEventProcessors(sequences, eventProcessorFactories);
    }

    /**
     * Set up a worker pool to handle events from the ring buffer. The worker pool will only process events
     * after every {@link EventProcessor} in this group has processed the event. Each event will be processed
     * by one of the work handler instances.
     *
     * <p>This method is generally used as part of a chain. For example if the handler <code>A</code> must
     * process events before the worker pool with handlers <code>B, C</code>:</p>
     *
     * <pre><code>dw.after(A).handleEventsWithWorkerPool(B, C);</code></pre>
     *
     * @param handlers the work handlers that will process events. Each work handler instance will provide an extra thread in the worker pool.
     * @return a {@link EventHandlerGroup} that can be used to set up a event processor barrier over the created event processors.
     */
    public EventHandlerGroup<T> handleEventsWithWorkerPool(final WorkHandler<? super T>... handlers) {
        return disruptor.createWorkerPool(sequences, handlers);
    }

    /**
     * Create a dependency barrier for the processors in this group.
     * This allows custom event processors to have dependencies on
     * {@link disruptor.sequence.BatchEventProcessor}s created by the disruptor.
     *
     * @return a {@link SequenceBarrier} including all the processors in this group.
     */
    public SequenceBarrier asSequenceBarrier() {
        return disruptor.getRingBuffer().newBarrier(sequences);
    }
}
