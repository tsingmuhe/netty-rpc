package com.sunchp.netty.rpc.core.transport.netty4;

import com.sunchp.netty.rpc.core.rpc.Request;
import com.sunchp.netty.rpc.core.rpc.Response;
import com.sunchp.netty.rpc.core.serialize.ProtobufSerializeUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class NettyServerChannelHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerChannelHandler.class);

    private final Map<String, Object> providers;

    public NettyServerChannelHandler(Map<String, Object> providers) {
        this.providers = providers;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof NettyMessage) {
            processMessage(ctx, (NettyMessage) msg);
        } else {
            LOGGER.error("NettyServerChannelHandler messageReceived type not support: class={}", msg.getClass());
        }
    }

    private void processMessage(ChannelHandlerContext ctx, NettyMessage msg) {
        Request request = ProtobufSerializeUtils.deserialize(msg.getData(), Request.class);
        Response response = new Response();
        response.setRequestId(request.getRequestId());

        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t.toString());
            LOGGER.error("RPC Server handle request error", t);
        }

        ctx.writeAndFlush(ProtobufSerializeUtils.serialize(response, Response.class))
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        LOGGER.debug("Send response for request " + request.getRequestId());
                    }
                });
    }


    private Object handle(Request request) throws Throwable {
        String className = request.getClassName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Object serviceBean = providers.get(className);
        Class<?> serviceClass = serviceBean.getClass();

        // JDK reflect
        Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("NettyServerChannelHandler channelActive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("NettyServerChannelHandler channelInactive: remote={} local={}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("NettyServerChannelHandler exceptionCaught: remote={} local={} event={}", ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause.getMessage(), cause);
        ctx.channel().close();
    }
}