package com.louis.server;


import com.louis.interfaces.Service;

public class ServiceImpl implements Service {

    @Override
    public String sayHello(String param) {
        return "你好," + param + ",很高兴认识你";
    }

    @Override
    public String sayGoodBye() {
        return "不好意思,我要走了，再见";
    }
}
