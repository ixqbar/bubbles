package cn.linjujia.web.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.linjujia.web.core.WebConfig;
import cn.linjujia.web.core.WebUtil;

/**
 * 
 */
public class WebSocketServerIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private final static Logger logger = LoggerFactory.getLogger(WebSocketServerIndexPageHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST), null);
            return;
        }

        // Allow only POST methods.
        if (req.method() != HttpMethod.POST) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN), null);
            return;
        }
        
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        if (!decoder.parameters().containsKey("token")) {
        	sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), "ERROR_TOKEN");
            return;
        }
        
        ByteBuf buf = req.content();
        if (buf.readableBytes() <= 0) {
        	sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), "ERROR_CONTENT");
            return;
        }
        
        byte [] contentBytes = new byte[buf.readableBytes()];    
        buf.readBytes(contentBytes);    
        String postMessage = new String(contentBytes,"UTF-8");
        
        logger.info("post {}, get {}", postMessage, req.uri());
        
        logger.info("tokenKey {}, token {}", WebConfig.tokenKey, decoder.parameters().get("token"));
        
        if (!WebUtil.md5(String.format("%s%s", postMessage, WebConfig.tokenKey).getBytes()).equals(decoder.parameters().get("token").get(0))) {
        	sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), "ERROR_TOKEN");
        	return;
        }
        
        //response OK
        sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK), "OK");
        
        //logic
        try {
        	WebSocketServerInitializer.executorService.submit(new WebSocketServerMessageHandler(ctx.channel(), postMessage, false));
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res, String content) {
    	ByteBuf buf;
    	
        if (res.status().code() != 200) {
            buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
        } else {
        	buf = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);
        }
        
        res.content().writeBytes(buf);
        buf.release();
        HttpUtil.setContentLength(res, res.content().readableBytes());

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
