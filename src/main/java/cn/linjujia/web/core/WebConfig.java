package cn.linjujia.web.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.netty.channel.Channel;

public class WebConfig {

	public static String tokenKey;
	
	public static void bootstrap() {
		tokenKey = System.getProperty("tokenKey");
	}
	
	public static Map<String, WebSocketClient> clientMapping = new HashMap<String, WebSocketClient>();
	public static Map<Channel, String> channelMapping = new HashMap<Channel, String>();
	
	public static Lock lock = new ReentrantLock();

	public static void clearChannelMapping(Channel channel, boolean toLock) {
		if (toLock) {
			WebConfig.lock.lock();
		}
		
		try {
			String uuid = WebConfig.channelMapping.remove(channel);
			if (uuid != null) {
				WebConfig.clientMapping.remove(uuid);
			}
		} finally {
			if (toLock) {
				WebConfig.lock.unlock();
			}
		}
	}
	
	public static void clearChannelMapping(Channel channel) {
		clearChannelMapping(channel, false);
	}
}
