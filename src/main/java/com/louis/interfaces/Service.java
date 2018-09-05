package com.louis.interfaces;

public interface Service {

    /**
     * 调用其它进程的api， 调用方不关心函数实现
     * @param param，入参
     * @return 远程服务的返回值
     */
    String sayHello(String param);

    String sayGoodBye();
}
