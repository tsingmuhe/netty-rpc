package com.sunchp.netty.rpc.core.api;

public interface HelloService {
    String hello(String name);

    String hello(Person person);

    Person hello1(String lastName, String firstname);
}
