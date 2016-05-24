package cn.linjujia.web.main;


import cn.linjujia.web.netty.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 *
 */
public class App {
	
	public static void main(String[] args) throws Exception {
		
		int port = 8080;
		final ServerBootstrap sb = new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
	    try {
	        sb.group(bossGroup, workerGroup)
	         .channel(NioServerSocketChannel.class)
	         .childHandler(new WebSocketServerInitializer());

	        final Channel ch = sb.bind(port).sync().channel();

	        ch.closeFuture().sync();
	    } finally {
	        workerGroup.shutdownGracefully();
	        bossGroup.shutdownGracefully();
	    }
	}
}
