package disruptor.sequence;

import disruptor.exception.AlertException;
import disruptor.exception.TimeoutException;
import disruptor.sequence.padding.Sequence;
import disruptor.strategy.WaitStrategy;

/**
 * {@link SequenceBarrier} handed out for gating {@link disruptor.event.EventProcessor}s
 * on a cursor sequence and optional dependent {@link disruptor.event.EventProcessor}(s),
 * using the given WaitStrategy.
 */
final class ProcessingSequenceBarrier implements SequenceBarrier {
    private final WaitStrategy waitStrategy;
    private final Sequence     dependentSequence;
    private volatile boolean   alerted = false;
    private final Sequence     cursorSequence;
    private final Sequencer    sequencer;

    public ProcessingSequenceBarrier(final Sequencer sequencer,
                                     final WaitStrategy waitStrategy,
                                     final Sequence cursorSequence,
                                     final Sequence[] dependentSequences) {
        this.sequencer = sequencer;
        this.waitStrategy = waitStrategy;
        this.cursorSequence = cursorSequence;
        if (0 == dependentSequences.length) {
            dependentSequence = cursorSequence;
        } else {
            dependentSequence = new FixedSequenceGroup(dependentSequences);
        }
    }

    @Override
    public long waitFor(final long sequence) throws AlertException, InterruptedException, TimeoutException {
        checkAlert();

        long availableSequence = waitStrategy.waitFor(sequence, cursorSequence, dependentSequence, this);

        if (availableSequence < sequence) {
            return availableSequence;
        }

        return sequencer.getHighestPublishedSequence(sequence, availableSequence);
    }

    @Override
    public long getCursor() {
        return dependentSequence.get();
    }

    @Override
    public boolean isAlerted() {
        return alerted;
    }

    @Override
    public void alert() {
        alerted = true;
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    public void clearAlert() {
        alerted = false;
    }

    @Override
    public void checkAlert() throws AlertException {
        if (alerted) {
            throw AlertException.INSTANCE;
        }
    }
}