package com.sunchp.netty.rpc.core.transport.netty4;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.sunchp.netty.rpc.core.RpcConstants.NETTY_HEADER;
import static com.sunchp.netty.rpc.core.RpcConstants.NETTY_MAGIC;

public class NettyDecoder extends ByteToMessageDecoder {
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < NETTY_HEADER) {
            return;
        }

        in.markReaderIndex();

        //magic 16bit
        short magic = in.readShort();
        if (magic != NETTY_MAGIC) {
            in.resetReaderIndex();
            throw new RuntimeException("NettyDecoder transport header not support, magic: " + magic);
        }

        int bodySize = in.readInt();
        byte[] data = new byte[bodySize];
        in.readBytes(data);
        out.add(new NettyMessage(data));
    }
}
