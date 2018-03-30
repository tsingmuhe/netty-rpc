package com.sunchp.netty.rpc.core.transport.netty4;

import com.sunchp.netty.rpc.core.rpc.Response;
import com.sunchp.netty.rpc.core.rpc.ResponsePromise;
import com.sunchp.netty.rpc.core.serialize.ProtobufSerializeUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

public class NettyClientChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientChannelHandler.class);

    private ConcurrentMap<String, ResponsePromise> callbackMap;

    public NettyClientChannelHandler(ConcurrentMap<String, ResponsePromise> callbackMap) {
        this.callbackMap = callbackMap;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyMessage) {
            processMessage(ctx, (NettyMessage) msg);
        } else {
            LOGGER.error("NettyClientChannelHandler messageReceived type not support: class={}", msg.getClass());
        }
    }

    private void processMessage(ChannelHandlerContext ctx, NettyMessage msg) {
        Response response = ProtobufSerializeUtils.deserialize(msg.getData(), Response.class);
        ResponsePromise responsePromise = callbackMap.remove(response.getRequestId());

        if (responsePromise == null) {
            LOGGER.warn("NettyClient has response from server, but responseFuture not exist, requestId={}", response.getRequestId());
            return;
        }
        if (response.getError() != null) {
            responsePromise.failed(new RuntimeException(response.getError()));
        } else {
            responsePromise.succeeded(response.getResult());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("NettyClientChannelHandler channelActive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("NettyClientChannelHandler channelInactive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("NettyClientChannelHandler exceptionCaught: remote={} local={} event={}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause.getMessage(), cause);
        ctx.channel().close();
    }
}
