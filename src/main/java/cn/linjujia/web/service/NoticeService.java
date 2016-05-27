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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class NoticeService extends WebSocketServerService {
	
	private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);
	
	public Object push(String uuid, String notice) throws Exception {
		if (uuid.isEmpty() 
				|| notice.isEmpty()) {
			throw new WebSokcetServerServiceException("error_token");
		}
		
		boolean success = false;
		try {
			for (Map.Entry<String, WebSocketClient> entry: WebConfig.clientMapping.entrySet()) {
				if (!entry.getValue().channel.isActive()) {
					continue;
				}
				
				if (entry.getKey().equals(uuid)) {
					WebSocketServerResponse webSocketServerResponse = new WebSocketServerResponse();
					webSocketServerResponse.n = true;
					webSocketServerResponse.c = 0;
					webSocketServerResponse.d = notice;
					webSocketServerResponse.i = entry.getValue().noticeIndex.getAndIncrement();
					entry.getValue().channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(webSocketServerResponse)));
					logger.info("push notice {} {}", uuid, notice);
					success = true;
					break;
				}
			}
		} finally {
			result.put("success", success);
		}
		
		return result;
	}
	
	public Object pushAll(String notice) throws Exception {
		if (notice.isEmpty()) {
			throw new WebSokcetServerServiceException("error_token");
		}
		
		int success = 0;
		try {
			for (Map.Entry<String, WebSocketClient> entry: WebConfig.clientMapping.entrySet()) {
				if (!entry.getValue().channel.isActive()) {
					continue;
				}
				
				WebSocketServerResponse webSocketServerResponse = new WebSocketServerResponse();
				webSocketServerResponse.n = true;
				webSocketServerResponse.c = 0;
				webSocketServerResponse.d = notice;
				webSocketServerResponse.i = entry.getValue().noticeIndex.getAndIncrement();
				entry.getValue().channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(webSocketServerResponse)));
				logger.info("push notice {} {} success", entry.getKey(), notice);
				success += 1;
			}
		} finally {
			result.put("success", success);
		}
		
		return result;
	}
}
