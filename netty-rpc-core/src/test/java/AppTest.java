import com.sunchp.netty.rpc.core.serialize.ProtobufSerializeUtils;
import com.sunchp.netty.rpc.core.transport.netty4.NettyClient;
import com.sunchp.netty.rpc.core.transport.netty4.NettyServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class AppTest {

    @Test
    public void server() throws InterruptedException {
        NettyServer chatServer = new NettyServer(8080);

        chatServer.open();

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void client() throws InterruptedException, UnsupportedEncodingException {
        NettyClient chatClient = new NettyClient("127.0.0.1", 8080);
        chatClient.open();
        Channel channel = chatClient.getChannel();
        ChannelFuture future = channel.writeAndFlush(ProtobufSerializeUtils.serialize("hello world", String.class));
        future.awaitUninterruptibly();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
