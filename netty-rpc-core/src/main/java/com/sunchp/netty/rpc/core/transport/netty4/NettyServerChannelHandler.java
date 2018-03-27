package com.sunchp.netty.rpc.core.transport.netty4;

import com.sunchp.netty.rpc.core.serialize.ProtobufSerializeUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyServerChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerChannelHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyMessage) {
            System.out.println(ProtobufSerializeUtils.deserialize(((NettyMessage) msg).getData(),String.class));
        } else {
            LOGGER.error("NettyServerChannelHandler messageReceived type not support: class={}", msg.getClass());
        }
    }
}
