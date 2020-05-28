package disruptor.event;

import disruptor.sequence.padding.Sequence;

/**
 * EventProcessors waitFor events to become available for consumption from the {@link disruptor.buffer.RingBuffer}
 * <p>
 * An EventProcessor will generally be associated with a Thread for execution.
 */
public interface EventProcessor extends Runnable {
    /**
     * Get a reference to the {@link Sequence} being used by this {@link EventProcessor}.
     *
     * @return reference to the {@link Sequence} for this {@link EventProcessor}
     */
    Sequence getSequence();

    /**
     * Signal that this EventProcessor should stop when it has finished consuming at the next clean break.
     * It will call {@link disruptor.sequence.SequenceBarrier#alert()} to notify the thread to check status.
     */
    void halt();

    boolean isRunning();
}
