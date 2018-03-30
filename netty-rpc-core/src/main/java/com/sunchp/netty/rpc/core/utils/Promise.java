package com.sunchp.netty.rpc.core.utils;

public interface Promise<C> {
    /**
     * <p>Callback invoked when the operation completes.</p>
     *
     * @param result the context
     * @see #failed(Throwable)
     */
    public abstract void succeeded(C result);

    /**
     * <p>Callback invoked when the operation fails.</p>
     *
     * @param x the reason for the operation failure
     */
    public void failed(Throwable x);
}
