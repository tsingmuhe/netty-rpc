package com.sunchp.netty.rpc.core.proxy.jdk;

import com.sunchp.netty.rpc.core.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class JdkProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clz, InvocationHandler invocationHandler) {
        return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, invocationHandler);
    }
}
