package com.wayyue.tracer.core.async;


import com.wayyue.tracer.core.context.trace.SzTraceContext;
import com.wayyue.tracer.core.holder.SzTraceContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class TracedExecutorService implements ExecutorService {

    protected final ExecutorService  delegate;
    protected final SzTraceContext traceContext;

    public TracedExecutorService(ExecutorService delegate) {
        this(delegate, SzTraceContextHolder.getSzTraceContext());
    }

    public TracedExecutorService(ExecutorService delegate, SzTraceContext traceContext) {
        this.delegate = delegate;
        this.traceContext = traceContext;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(new SzTracerCallable<T>(task, traceContext));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(new SzTracerRunnable(task, traceContext), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(new SzTracerRunnable(task, traceContext));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
                                                                                 throws InterruptedException {
        return delegate.invokeAll(wrapTracerCallableCollection(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                         TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(wrapTracerCallableCollection(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
                                                                   ExecutionException {
        return delegate.invokeAny(wrapTracerCallableCollection(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                                                                                                throws InterruptedException,
                                                                                                ExecutionException,
                                                                                                TimeoutException {
        return delegate.invokeAny(wrapTracerCallableCollection(tasks), timeout, unit);
    }

    @Override
    public void execute(final Runnable command) {
        delegate.execute(new SzTracerRunnable(command, traceContext));
    }

    private <T> Collection<? extends Callable<T>> wrapTracerCallableCollection(Collection<? extends Callable<T>> originalCollection) {
        Collection<Callable<T>> collection = new ArrayList<Callable<T>>(
            originalCollection.size());
        for (Callable<T> c : originalCollection) {
            collection.add(new SzTracerCallable<T>(c, traceContext));
        }
        return collection;
    }
}
