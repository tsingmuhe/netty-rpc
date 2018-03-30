package com.sunchp.netty.rpc.core.transport.netty4;

import com.sunchp.netty.rpc.core.transport.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NettyServer implements Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);

    public static final int DEFAULT_PORT = 8080;

    private int port = DEFAULT_PORT;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    protected Map<String, Object> providers = new HashMap<>();


    public NettyServer() {
    }

    public NettyServer(int port) {
        this.port = port;
    }

    public void open() {
        if (bossGroup == null) {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
        }

        LOGGER.info("NettyServer start Open: port={}", port);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new NettyDecoder());
                        pipeline.addLast("encoder", new NettyEncoder());
                        pipeline.addLast("handler", new NettyServerChannelHandler(providers));
                    }
                });

        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture channelFuture = serverBootstrap.bind(port);
        channelFuture.syncUninterruptibly();
        serverChannel = channelFuture.channel();

        LOGGER.info("NettyServer finish Open: port={}", port);
    }

    public void close() {
        try {
            // close listen socket
            if (serverChannel != null) {
                serverChannel.close();
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                bossGroup = null;
                workerGroup = null;
            }

            LOGGER.info("NettyServer close Success: port={}", port);
        } catch (Exception e) {
            LOGGER.error("NettyServer close Error: port={}", port, e);
        }
    }

    public NettyServer addService(String interfaceName, Object serviceBean) {
        if (!providers.containsKey(interfaceName)) {
            LOGGER.info("Loading service: {}", interfaceName);
            providers.put(interfaceName, serviceBean);
        }

        return this;
    }
}
