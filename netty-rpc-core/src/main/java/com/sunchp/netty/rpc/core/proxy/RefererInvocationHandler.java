package com.sunchp.netty.rpc.core.proxy;

import com.sunchp.netty.rpc.core.rpc.Request;
import com.sunchp.netty.rpc.core.rpc.ResponsePromise;
import com.sunchp.netty.rpc.core.transport.Client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class RefererInvocationHandler<T> implements InvocationHandler {
    private Class<T> clz;
    private Client client;

    public RefererInvocationHandler(Class<T> clz, Client client) {
        this.clz = clz;
        this.client = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isLocalMethod(method)) {
            throw new RuntimeException("can not invoke local method:" + method.getName());
        }

        Request request = new Request();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        ResponsePromise responsePromise = client.request(request);
        return responsePromise.get();
    }

    /**
     * tostring,equals,hashCode,finalize等接口未声明的方法不进行远程调用
     *
     * @param method
     * @return
     */
    public boolean isLocalMethod(Method method) {
        if (method.getDeclaringClass().equals(Object.class)) {
            try {
                Method interfaceMethod = clz.getDeclaredMethod(method.getName(), method.getParameterTypes());
                return false;
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }
}
