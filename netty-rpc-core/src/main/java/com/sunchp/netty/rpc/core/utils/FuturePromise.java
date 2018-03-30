package com.sunchp.netty.rpc.core.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FuturePromise<C> implements Future<C>, Promise<C> {
    private static Throwable COMPLETED = new ConstantThrowable();

    private final AtomicBoolean _done = new AtomicBoolean(false);
    private final CountDownLatch _latch = new CountDownLatch(1);

    private Throwable _cause;
    private C _result;

    @Override
    public void succeeded(C result) {
        if (_done.compareAndSet(false, true)) {
            _result = result;
            _cause = COMPLETED;
            _latch.countDown();
        }
    }

    @Override
    public void failed(Throwable cause) {
        if (_done.compareAndSet(false, true)) {
            _cause = cause;
            _latch.countDown();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (_done.compareAndSet(false, true)) {
            _result = null;
            _cause = new CancellationException();
            _latch.countDown();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        if (_done.get()) {
            try {
                _latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return _cause instanceof CancellationException;
        }
        return false;
    }

    @Override
    public boolean isDone() {
        return _done.get() && _latch.getCount() == 0;
    }

    @Override
    public C get() throws InterruptedException, ExecutionException {
        _latch.await();
        if (_cause == COMPLETED) {
            return _result;
        }

        if (_cause instanceof CancellationException) {
            throw (CancellationException) new CancellationException().initCause(_cause);
        }

        throw new ExecutionException(_cause);
    }

    @Override
    public C get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!_latch.await(timeout, unit)) {
            throw new TimeoutException();
        }

        if (_cause == COMPLETED) {
            return _result;
        }

        if (_cause instanceof TimeoutException) {
            throw (TimeoutException) _cause;
        }

        if (_cause instanceof CancellationException) {
            throw (CancellationException) new CancellationException().initCause(_cause);
        }

        throw new ExecutionException(_cause);
    }
}
