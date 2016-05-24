package cn.linjujia.web.core;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;

public class WebSocketClient {
	
	public Channel channel;
	public AtomicInteger noticeIndex = new AtomicInteger();
	
}
