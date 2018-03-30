package com.sunchp.netty.rpc.core.server;

import com.sunchp.netty.rpc.core.api.HelloService;
import com.sunchp.netty.rpc.core.api.Person;

public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }

    @Override
    public Person hello1(String lastName, String firstname) {
        return new Person(lastName, firstname);
    }
}
