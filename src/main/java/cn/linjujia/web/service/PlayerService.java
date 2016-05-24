package cn.linjujia.web.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.linjujia.web.core.WebConfig;
import cn.linjujia.web.core.WebSocketClient;
import cn.linjujia.web.core.WebSocketServerResponse;
import cn.linjujia.web.core.WebSocketServerService;
import cn.linjujia.web.core.WebSokcetServerServiceException;
import cn.linjujia.web.core.WebUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PlayerService extends WebSocketServerService {
	
	private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

	public Object login(String uuid, String token) throws Exception {
		if (uuid.isEmpty() 
				|| token.isEmpty()) {
			throw new Exception("error_token");
		}
		
		if (!WebUtil.md5(String.format("%s%s", uuid, WebConfig.tokenKey).getBytes()).equals(token)) {
			throw new Exception("error_token");
		}
		
		WebConfig.lock.lock();
		try {
			WebSocketClient webSocketClient = WebConfig.clientMapping.get(uuid);
			if (webSocketClient != null) {
				WebConfig.clearChannelMapping(webSocketClient.channel);
				webSocketClient.channel.close();
			} else {
				webSocketClient = new WebSocketClient();
			}
			
			webSocketClient.channel = channel;
			webSocketClient.noticeIndex.incrementAndGet();
			
			WebConfig.clientMapping.put(uuid, webSocketClient);
			WebConfig.channelMapping.put(channel, uuid);
		} finally {
			WebConfig.lock.unlock();
		}
		
		result.put("success", true);
		
		return result;
	}
	
	public Object notice(String uuid, String notice) throws Exception {
		if (uuid.isEmpty() 
				|| notice.isEmpty()) {
			throw new WebSokcetServerServiceException("error_token");
		}
		
		for (Map.Entry<String, WebSocketClient> entry: WebConfig.clientMapping.entrySet()) {
			if (entry.getKey().equals(uuid)) {
				WebSocketServerResponse webSocketServerResponse = new WebSocketServerResponse();
				webSocketServerResponse.n = true;
				webSocketServerResponse.c = 0;
				webSocketServerResponse.d = notice;
				webSocketServerResponse.i = entry.getValue().noticeIndex.getAndIncrement();
				entry.getValue().channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(webSocketServerResponse)));
				logger.info("push notice {} {}", uuid, notice);
				break;
			}
		}
		
		result.put("success", true);
		
		return result;
	}
}
