package com.louis.registrycenter;

import com.alibaba.fastjson.JSONObject;
import com.louis.interfaces.RegistryService;
import com.louis.interfaces.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RegistryCenter implements RegistryService {

    private final ExecutorService executorService = new ThreadPoolExecutor(5,
                                  200, 0L, TimeUnit.MILLISECONDS,
                                   new LinkedBlockingDeque<Runnable>(1024));

    public static final HashMap<String, ServiceInfo> services = new HashMap<>();  //注册服务的信息

    private int port;

    public class ServiceInfo {
        Class service;  //接口
        Class impl;
        String ip;
        int port;

        public Class getService() {
            return service;
        }

        public Class getImpl() {
            return impl;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }
    }
    public RegistryCenter(int port) {
        this.port = port;
    }

    @Override
    public void registerService(Class service, Class impl,  String ip, int port) {
        ServiceInfo info = new ServiceInfo();
        info.service = service;
        info.impl = impl;
        info.ip = ip;
        info.port = port;

        services.put(service.getName(), info);
    }

    @Override
    public boolean start() throws IOException{
        if (services.isEmpty()) {
            return false;
        }

        ServerSocket server = null;
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(port));
            System.out.println("start server");

            while (true) {
                executorService.execute(new ProviderTask(server.accept()));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (server != null) {
                server.close();
            }
        }

        return true;
    }


    //生产者(接口实现者)的逻辑, 基于短连接
    class ProviderTask implements Runnable {
        Socket server = null;

        public ProviderTask(Socket socket) {
            server = socket;
        }

        @Override
        public void run() {
            ObjectInputStream input = null;
            ObjectOutputStream output = null;
            try {
                //从socket字节流转换成要调用的API
                input = new ObjectInputStream(server.getInputStream());

                // 获取请求信息
                RpcRequest request = (RpcRequest) input.readObject();
                System.out.println(JSONObject.toJSON(request));
                String serviceName = request.getServiceName();
                String methodName = request.getMethodName();
                Class<?>[] paramTypes = request.getParamTypes();
                Object[] arguments = request.getArguments();

                ServiceInfo info = services.get(serviceName);
                if (info == null) {
                    throw new Exception("没有该服务" + serviceName);
                }
                Class clz = info.impl;
                Method method = clz.getMethod(methodName, paramTypes);  //找到方法
                Object result = method.invoke(clz.newInstance(), arguments);   //执行函数

                output = new ObjectOutputStream(server.getOutputStream());
                output.writeObject(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                //释放资源
                if (output != null) {
                    try {
                        output.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (input != null) {
                    try {
                        input.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (server != null) {
                    try {
                        server.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
