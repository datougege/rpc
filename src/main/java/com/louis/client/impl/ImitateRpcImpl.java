package com.louis.client.impl;


import com.louis.client.ImitateRpc;

public class ImitateRpcImpl implements ImitateRpc {

    @Override
    public String callRemote(String param) {
        System.out.println("Hi " + param + ", 你调用了RPC方法");
        return "success";
    }
}
