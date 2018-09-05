package com.louis.netty;


import com.alibaba.fastjson.JSONObject;
import com.louis.client.impl.ImitateRpcImpl;
import com.louis.interfaces.RpcRequest;
import com.louis.registrycenter.RegistryCenter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        String request = new String(data, "utf-8");
        RpcRequest r =  JSONObject.parseObject(request,RpcRequest.class);

        Class clz = ImitateRpcImpl.class;
        Method method = clz.getMethod(r.getMethodName(), r.getParamTypes());  //找到方法
        Object result = method.invoke(clz.newInstance(), r.getArguments());   //执行函数

        //有write操作则可以不释放msg
        ctx.writeAndFlush(Unpooled.copiedBuffer(JSONObject.toJSONString(result).getBytes()));//加上.addListener(ChannelFutureListener.CLOSE);则表示写回数据后断开客户端的链接

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel active...");
    }

}
