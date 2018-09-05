package com.louis.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    public static void main(String[] args) throws Exception {
        //1 第一个线程组 是用于接收Client端连接的
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //2 第二个线程组 是用于实际的业务处理操作的
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        //3 创建一个辅助类Bootstrap，就是对我们的Server进行一系列的配置
        ServerBootstrap b = new ServerBootstrap();
        //把俩个工作线程组加入进来
        b.group(bossGroup, workerGroup)
                //我要指定使用NioServerSocketChannel这种类型的通道
                .channel(NioServerSocketChannel.class)
                //一定要使用 childHandler 去绑定具体的 事件处理器
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ServerHandler()); //ServerHandler为我们自定义的事件处理类
                    }
                })
                .option(ChannelOption.SO_SNDBUF, 50*1024)   //设置发送缓冲大小
                .option(ChannelOption.SO_RCVBUF,50*1024);  //是指接受缓冲大小

        System.out.println("服务端启动...");
        //绑定指定的端口 进行监听
        ChannelFuture f = b.bind(8765).sync();

        //Thread.sleep(1000000);
        f.channel().closeFuture().sync();  //没有这句话则不会在此阻塞等待客户端的连接，而是直接执行后面代码

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

    }

}
