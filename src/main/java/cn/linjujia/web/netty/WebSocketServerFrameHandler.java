package cn.linjujia.web.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.linjujia.web.core.WebConfig;

/**
 * 
 */
public class WebSocketServerFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    
	private static final Logger logger = LoggerFactory.getLogger(WebSocketServerFrameHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String frameMessage = ((TextWebSocketFrame) frame).text();
            logger.info("{} received {}", ctx.channel().remoteAddress(), frameMessage);
            
            try {
            	WebSocketServerInitializer.executorService.submit(new WebSocketServerMessageHandler(ctx.channel(), frameMessage));
            } catch (Exception e) {
            	e.printStackTrace();
            	ctx.channel().close();
            }
        } else {
            logger.error("unsupported frame type: " + frame.getClass().getName());
            ctx.channel().close();
        }
    }

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			switch (((IdleStateEvent) evt).state()) {
				case WRITER_IDLE:
					ctx.channel().writeAndFlush(new PingWebSocketFrame());
					break;
				default:
					logger.info("{} timeout!", ctx.channel().remoteAddress());
					WebConfig.clearChannelMapping(ctx.channel(), true);
					ctx.channel().close();
					break;
			}
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		logger.info("{} conected!", ctx.channel().remoteAddress());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		logger.info("{} disconect!", ctx.channel().remoteAddress());
	}
    
}
