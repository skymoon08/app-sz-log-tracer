package disruptor.sequence;


import disruptor.sequence.padding.Sequence;
import disruptor.util.Util;

import java.util.Arrays;

/**
 * Hides a group of Sequences behind a single Sequence
 */
public final class FixedSequenceGroup extends Sequence {
    private final Sequence[] sequences;

    /**
     * Constructor
     *
     * @param sequences the list of sequences to be tracked under this sequence group
     */
    public FixedSequenceGroup(Sequence[] sequences) {
        this.sequences = Arrays.copyOf(sequences, sequences.length);
    }

    /**
     * Get the minimum sequence value for the group.
     *
     * @return the minimum sequence value for the group.
     */
    @Override
    public long get() {
        return Util.getMinimumSequence(sequences);
    }

    @Override
    public String toString() {
        return Arrays.toString(sequences);
    }

    /**
     * Not supported.
     */
    @Override
    public void set(long value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public boolean compareAndSet(long expectedValue, long newValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public long incrementAndGet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public long addAndGet(long increment) {
        throw new UnsupportedOperationException();
    }
}
