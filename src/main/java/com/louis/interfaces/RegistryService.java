package com.louis.interfaces;

import java.io.IOException;

//服务注册中心
public interface RegistryService {

    /**
     * 注册服务， 注意：同一个服务可运行在多台机器或同一台机器的不同端口上。
     * @param service， 需要实现的接口
     * @param impl， 接口实现
     * @param ip， 服务运行机器的ip地址
     * @param port，服务运行机器的端口
     */
    void registerService(Class service,
                         Class impl,
                         String ip,
                         int port);

    boolean start()  throws IOException;    //启动注册中心

}
