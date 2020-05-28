package disruptor.sequence;

/**
 * Implementors of this interface must provide a single long value
 * that represents their current cursor value.  Used during dynamic
 * add/remove of Sequences from a
 * {@link disruptor.sequence.group.SequenceGroups#addSequences(Object, java.util.concurrent.atomic.AtomicReferenceFieldUpdater, Cursored, Sequence...)}.
 */
public interface Cursored {
    /**
     * Get the current cursor value.
     *
     * @return current cursor value
     */
    long getCursor();
}
