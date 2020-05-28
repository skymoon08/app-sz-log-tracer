package disruptor.dsl.worker;


import disruptor.dsl.ConsumerInfo;
import disruptor.sequence.SequenceBarrier;
import disruptor.sequence.padding.Sequence;

import java.util.concurrent.Executor;

public class WorkerPoolInfo<T> implements ConsumerInfo {

    private final WorkerPool<T> workerPool;
    private final SequenceBarrier sequenceBarrier;
    private boolean endOfChain = true;

    public WorkerPoolInfo(final WorkerPool<T> workerPool, final SequenceBarrier sequenceBarrier) {
        this.workerPool = workerPool;
        this.sequenceBarrier = sequenceBarrier;
    }

    @Override
    public Sequence[] getSequences() {
        return workerPool.getWorkerSequences();
    }

    @Override
    public SequenceBarrier getBarrier() {
        return sequenceBarrier;
    }

    @Override
    public boolean isEndOfChain() {
        return endOfChain;
    }

    @Override
    public void start(final Executor executor) {
        workerPool.start(executor);
    }

    @Override
    public void halt() {
        workerPool.halt();
    }

    @Override
    public void markAsUsedInBarrier() {
        endOfChain = false;
    }

    @Override
    public boolean isRunning() {
        return workerPool.isRunning();
    }
}
