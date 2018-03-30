package com.sunchp.netty.rpc.core.transport;

import com.sunchp.netty.rpc.core.rpc.Request;
import com.sunchp.netty.rpc.core.rpc.ResponsePromise;

public interface Client extends Endpoint {
    ResponsePromise request(Request request) throws TransportException;
}
