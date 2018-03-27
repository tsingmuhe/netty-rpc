package com.sunchp.netty.rpc.core.transport;

public interface Endpoint {
    /**
     * open the channel
     */
    void open();

    /**
     * close the channel.
     */
    void close();
}
