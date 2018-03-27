package com.sunchp.netty.rpc.core.transport.netty4;

public class NettyMessage {
    private byte[] data;

    public NettyMessage(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
