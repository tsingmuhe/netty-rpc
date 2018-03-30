package com.sunchp.netty.rpc.core;

import com.sunchp.netty.rpc.core.api.HelloService;
import com.sunchp.netty.rpc.core.api.Person;
import com.sunchp.netty.rpc.core.proxy.ProxyFactory;
import com.sunchp.netty.rpc.core.proxy.RefererInvocationHandler;
import com.sunchp.netty.rpc.core.proxy.jdk.JdkProxyFactory;
import com.sunchp.netty.rpc.core.server.HelloServiceImpl;
import com.sunchp.netty.rpc.core.transport.netty4.NettyClient;
import com.sunchp.netty.rpc.core.transport.netty4.NettyServer;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class AppTest {

    @Test
    public void server() throws InterruptedException {
        NettyServer rpcServer = new NettyServer(8080);

        rpcServer.open();

        HelloService helloService = new HelloServiceImpl();
        rpcServer.addService("com.sunchp.netty.rpc.core.api.HelloService", helloService);

        Thread.sleep(Integer.MAX_VALUE);
    }

    @Test
    public void client() throws InterruptedException, UnsupportedEncodingException {
        NettyClient rpcClient = new NettyClient("127.0.0.1", 8080);
        rpcClient.open();

        ProxyFactory proxyFactory = new JdkProxyFactory();
        HelloService helloService = proxyFactory.getProxy(HelloService.class, new RefererInvocationHandler<HelloService>(HelloService.class, rpcClient));

        String result1 = helloService.hello("sunchp");
        System.out.println("1======" + result1);

        Person person = new Person();
        person.setFirstName("changpeng");
        person.setLastName("sun");

        String result2 = helloService.hello(person);
        System.out.println("2======" + result2);

        Person person1 = helloService.hello1("liuliu", "yang");

        System.out.println(person1);

        Thread.sleep(Integer.MAX_VALUE);
    }
}
