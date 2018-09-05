package com.louis.interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

//客户端的远程代理
public class RpcClient {
    /**
     * 获取远程服务的代理对象
     * @param service， 接口
     * @param addr，远程服务运行端口
     * @return 对象
     */
    public static <T> T getRpcProxyObj(final Class<?> service, final InetSocketAddress addr) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Socket socket = null;
                        ObjectOutputStream outputStream = null;
                        ObjectInputStream inputStream = null;

                        try {
                            socket = new Socket();
                            socket.connect(addr);
                            outputStream = new ObjectOutputStream(socket.getOutputStream());

                            // 组装请求信息
                            RpcRequest r = new RpcRequest();
                            r.setServiceName(service.getName());
                            r.setMethodName(method.getName());
                            r.setParamTypes(method.getParameterTypes());
                            r.setArguments(args);
                            outputStream.writeObject(r);

                            //阻塞等待返回
                            inputStream = new ObjectInputStream(socket.getInputStream());
                            Object obj = inputStream.readObject();

                            return obj;   //类对象
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            if (socket != null) {
                                socket.close();
                            }
                            if (inputStream != null) {
                                inputStream.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                        return null;
                    }
                });
    }
}
