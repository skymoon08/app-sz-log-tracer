package disruptor.strategy;

import disruptor.exception.AlertException;
import disruptor.exception.TimeoutException;
import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

/**
 * Strategy employed for making {@link disruptor.event.EventProcessor}s wait on a cursor {@link Sequence}.
 */
public interface WaitStrategy {
    /**
     * Wait for the given sequence to be available.  It is possible for this method to return a value
     * less than the sequence number supplied depending on the implementation of the WaitStrategy.  A common
     * use for this is to signal a timeout.  Any EventProcessor that is using a WaitStrategy to get notifications
     * about message becoming available should remember to handle this case.  The {@link disruptor.sequence.BatchEventProcessor} explicitly
     * handles this case and will signal a timeout if required.
     *
     * @param sequence          to be waited on.
     * @param cursor            the main sequence from ringbuffer. Wait/notify strategies will
     *                          need this as it's the only sequence that is also notified upon update.
     * @param dependentSequence on which to wait.
     * @param barrier           the processor is waiting on.
     * @return the sequence that is available which may be greater than the requested sequence.
     * @throws AlertException       if the status of the Disruptor has changed.
     * @throws InterruptedException if the thread is interrupted.
     * @throws java.util.concurrent.TimeoutException TimeoutException
     */
    long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)
            throws AlertException, InterruptedException, TimeoutException;

    /**
     * Implementations should signal the waiting {@link disruptor.event.EventProcessor}s that the cursor has advanced.
     */
    void signalAllWhenBlocking();
}
