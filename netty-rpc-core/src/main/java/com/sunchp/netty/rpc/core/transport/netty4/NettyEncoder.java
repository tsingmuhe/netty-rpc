package com.sunchp.netty.rpc.core.transport.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import static com.sunchp.netty.rpc.core.RpcConstants.NETTY_MAGIC;

public class NettyEncoder extends MessageToByteEncoder<byte[]> {
    protected void encode(ChannelHandlerContext ctx, byte[] in, ByteBuf out) throws Exception {
        out.writeShort(NETTY_MAGIC);
        out.writeInt(in.length);
        out.writeBytes(in);
    }
}
