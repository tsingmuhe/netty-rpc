package com.sunchp.netty.rpc.core.transport.netty4;

import com.sunchp.netty.rpc.core.rpc.Request;
import com.sunchp.netty.rpc.core.rpc.ResponsePromise;
import com.sunchp.netty.rpc.core.serialize.ProtobufSerializeUtils;
import com.sunchp.netty.rpc.core.transport.Client;
import com.sunchp.netty.rpc.core.transport.TransportException;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyClient.class);

    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();

    private String host;
    private int port;
    private Bootstrap bootstrap;
    private Channel channel = null;

    private final ConcurrentMap<String, ResponsePromise> callbackMap = new ConcurrentHashMap<String, ResponsePromise>();


    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Channel getChannel() {
        return channel;
    }

    public void open() {
        LOGGER.info("NettyClient start Open: host={},port={}", host, port);

        bootstrap = new Bootstrap();

        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new NettyDecoder());
                        pipeline.addLast("encoder", new NettyEncoder());
                        pipeline.addLast("handler", new NettyClientChannelHandler(callbackMap));
                    }
                });

        ChannelFuture channelFuture = bootstrap.connect(host, port).syncUninterruptibly();
        channel = channelFuture.channel();

        LOGGER.info("NettyClient finish Open: host={},port={}", host, port);
    }

    public void close() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception e) {
            LOGGER.error("NettyChannel close Error.", e);
        }
    }

    @Override
    public ResponsePromise request(Request request) throws TransportException {
        ResponsePromise responsePromise = new ResponsePromise(request);
        callbackMap.put(request.getRequestId(), responsePromise);

        ChannelFuture writeFuture = this.channel.writeAndFlush(ProtobufSerializeUtils.serialize(request, Request.class));
        boolean result = writeFuture.awaitUninterruptibly(200, TimeUnit.MILLISECONDS);

        if (result && writeFuture.isSuccess()) {
            return responsePromise;
        }

        writeFuture.cancel(true);
        callbackMap.remove(request.getRequestId());
        responsePromise.cancel(true);

        if (writeFuture.cause() != null) {
            throw new TransportException("NettyChannel send request to server Error.", writeFuture.cause());
        }

        throw new TransportException("NettyChannel send request to server Timeout.");
    }
}
