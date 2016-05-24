package cn.linjujia.web.main;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.linjujia.web.core.WebConfig;
import cn.linjujia.web.netty.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 
 * 
 */
public class App {
	
	private final static Logger logger = LoggerFactory.getLogger(App.class);
			
	public static void main(String[] args) throws Exception {
		int port = args.length != 1 ? 8080 : Integer.parseInt(args[0]);
		
		WebConfig.bootstrap();
		
		final ServerBootstrap sb = new ServerBootstrap();
		EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
	    try {
	        sb.group(bossGroup, workerGroup)
	         .channel(NioServerSocketChannel.class)
	         .childHandler(new WebSocketServerInitializer());

	        final Channel ch = sb.bind(port).sync().channel();

	        logger.info("websocket run at {}", port);
	        
	        ch.closeFuture().sync();
	    } finally {
	        workerGroup.shutdownGracefully();
	        bossGroup.shutdownGracefully();
	    }
	}
}
