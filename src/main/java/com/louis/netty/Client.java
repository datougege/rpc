package com.louis.netty;

import com.alibaba.fastjson.JSONObject;
import com.louis.client.ImitateRpc;
import com.louis.client.impl.ImitateRpcImpl;
import com.louis.interfaces.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Client {

    public static void main(String[] args) throws Exception {

        EventLoopGroup workgroup = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workgroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ClientHandler());
                    }
                });
        System.out.println("客户端连接...");
        ChannelFuture cf1 = b.connect("127.0.0.1", 8765).sync();
        invoke(cf1.channel());
        cf1.channel().closeFuture().sync();
        workgroup.shutdownGracefully();

    }

    private static void invoke(Channel channel) throws NoSuchMethodException {

        // 组装请求信息
        Method method = ImitateRpcImpl.class.getMethod("callRemote", String.class);  //模拟RPC调用的API
        Class svrClass = ImitateRpc.class;  //实现类
        Object[] arguments = new Object[1];
        arguments[0] = "Louis";

        RpcRequest r = new RpcRequest();
        r.setServiceName(svrClass.getName());
        r.setMethodName(method.getName());
        r.setParamTypes(method.getParameterTypes());
        r.setArguments(arguments);

        channel.writeAndFlush(Unpooled.copiedBuffer(JSONObject.toJSONString(r).getBytes()));
    }

    public static <T> T getRpcProxyObj(final Class service, final Channel channel){
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {

                        // 组装请求信息
                        Class svrClass = ImitateRpc.class;  //实现类
                        Object[] arguments = new Object[1];
                        arguments[0] = "Louis";

                        RpcRequest r = new RpcRequest();
                        r.setServiceName(svrClass.getName());
                        r.setMethodName(method.getName());
                        r.setParamTypes(method.getParameterTypes());
                        r.setArguments(arguments);

                        System.out.println(JSONObject.toJSONString(r));
                        channel.writeAndFlush(Unpooled.copiedBuffer(JSONObject.toJSONString(r).getBytes()));

                        return null;
                    }
                });
    }


}

