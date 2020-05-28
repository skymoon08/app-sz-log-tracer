package disruptor.dsl;

import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

import java.util.concurrent.Executor;

public interface ConsumerInfo {
    Sequence[] getSequences();

    SequenceBarrier getBarrier();

    boolean isEndOfChain();

    void start(Executor executor);

    void halt();

    void markAsUsedInBarrier();

    boolean isRunning();
}
