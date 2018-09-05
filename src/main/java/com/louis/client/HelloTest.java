package com.louis.client;


import com.louis.interfaces.Service;
import com.louis.interfaces.RpcClient;

import java.net.InetSocketAddress;

public class HelloTest {
    public static void main(String[] args) {

        Service service = RpcClient.getRpcProxyObj(Service.class,
                new InetSocketAddress("localhost", 9011));
        System.out.println(service.sayHello("陈伟成"));

    }
}
