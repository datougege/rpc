package com.louis.client;



import com.louis.client.impl.ImitateRpcImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ObjectTest {

    public static void main(String[] args) throws Exception {
        //methodInvoke();
        try {
            bytes2Object();
            testCallRemote();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // 通过反射在运行创建一个对象调用方法
    private static void methodInvoke() throws Exception {
        Method method = ImitateRpc.class.getMethod("callRemote", String.class);  //模拟RPC调用的API
        Class svrClass = ImitateRpcImpl.class;  //实现类
        Method svrMethod = svrClass.getMethod(method.getName(), method.getParameterTypes());
        Object[] arguments = new Object[1];
        arguments[0] = "Louis";

        Object result = svrMethod.invoke(svrClass.newInstance(), arguments);
        System.out.println(result);
    }

    //解释RPC原理， 即客户端执行接口时是如何找到服务端的函数体的
    private static void testCallRemote() throws Exception {
        ObjectOutputStream outputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        outputStream = new ObjectOutputStream(byteArrayOutputStream);

        Method method = ImitateRpc.class.getMethod("callRemote", String.class);  //模拟RPC调用的API

        //模拟客户端发送数据
        outputStream.writeUTF(ImitateRpc.class.getName());   //接口类名
        outputStream.writeUTF(method.getName()); //方法名称
        outputStream.writeObject(method.getParameterTypes()); //方法参数类型
        Object[] args = new Object[1];
        args[0] = "Louis";
        outputStream.writeObject(args);   //方法参数

        /**

         * 此处省略了客户端发送字节流到服务端和服务端接收字节流的过程
         */

        //模拟服务端接收到数据
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        ObjectInputStream serverInputStream = new ObjectInputStream(inputStream);
        String serviceName = serverInputStream.readUTF();  //接口类
        String methodName = serverInputStream.readUTF();
        Class<?>[] paramTypes = (Class<?>[]) serverInputStream.readObject();
        Object[] arguments = (Object[]) serverInputStream.readObject();

        Class svrClass = ImitateRpcImpl.class;  //实现类
        Method svrMethod = svrClass.getMethod(methodName, paramTypes);
        Object result = svrMethod.invoke(svrClass.newInstance(), arguments);

        //省略将数据返回到客户端， 代码同上

    }

    //Java对象和字节流之间互相转换
    private static void bytes2Object() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("name", "Tom");
        map.put("age", "20");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(map);  //将Java对象转换成流

        byte[] bytes = byteArrayOutputStream.toByteArray();  //将map对象转换成字节流
        System.out.println("我就是map对象的字节流：");
        System.out.println(new String(bytes, "utf-8"));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Map<String, String> objMap = (Map<String, String>)objectInputStream.readObject();
        System.out.println("字节流转换成map对象，大小：" + objMap.size());
    }
}
