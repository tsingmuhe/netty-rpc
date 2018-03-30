package com.sunchp.netty.rpc.core.rpc;

import com.sunchp.netty.rpc.core.utils.FuturePromise;

public class ResponsePromise extends FuturePromise {
    private Request request;

    public ResponsePromise(Request request) {
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }
}
