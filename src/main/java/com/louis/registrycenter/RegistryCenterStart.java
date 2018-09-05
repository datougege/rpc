package com.louis.registrycenter;

import com.louis.interfaces.Service;
import com.louis.interfaces.RegistryService;
import com.louis.server.ServiceImpl;

public class RegistryCenterStart {
    public static void main(String[] args) {
        try {
            RegistryService registryService = new RegistryCenter(9011);
            registryService.registerService(Service.class,
                    ServiceImpl.class,// 消费者
                    "localhost",
                    9011);
            registryService.start();    //启动注册中心
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
