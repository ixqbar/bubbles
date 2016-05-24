package cn.linjujia.web.core;

import com.alibaba.fastjson.JSONObject;

import io.netty.channel.Channel;

public class WebSocketServerService {

	public Channel channel;
	public boolean isWebSocketClient;
	public int timestamp;
	public String ip;
	
	public JSONObject result = new JSONObject();
	
}
