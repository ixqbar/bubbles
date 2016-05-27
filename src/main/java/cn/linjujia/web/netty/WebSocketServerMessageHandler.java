package cn.linjujia.web.netty;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import cn.linjujia.web.core.WebSocketServerRequest;
import cn.linjujia.web.core.WebSocketServerResponse;
import cn.linjujia.web.core.WebSocketServerService;
import cn.linjujia.web.core.WebSocketServerServiceMapping;
import cn.linjujia.web.core.WebUtil;
import cn.linjujia.web.core.WebSokcetServerServiceException;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WebSocketServerMessageHandler implements Runnable {
	
	private final static Logger logger = LoggerFactory.getLogger(WebSocketServerMessageHandler.class);
	
	private boolean isWebSocketClient = true;
	private Channel channel = null;
	private String message = null;
	
	public WebSocketServerMessageHandler(Channel channel, String message) {
		this.channel = channel;
		this.message = message;
	}
	
	public WebSocketServerMessageHandler(Channel channel, String message, boolean isWebSocketClient) {
		this(channel, message);
		this.isWebSocketClient = isWebSocketClient;
	}

	@Override
	public void run() {
		boolean doFinally = true;
		WebSocketServerRequest webSocketServerRequest = null;
		WebSocketServerResponse webSocketServerResponse = new WebSocketServerResponse();
		webSocketServerResponse.n = false;
		webSocketServerResponse.c = 0;
		webSocketServerResponse.m = "";
		webSocketServerResponse.i = 0;
		
		try {
			if (message.isEmpty()) {
				throw new Exception("error_message");
			}
			
			webSocketServerRequest = JSON.parseObject(message, WebSocketServerRequest.class);
			if (webSocketServerRequest.s == null
					|| webSocketServerRequest.s.isEmpty()
					|| webSocketServerRequest.s.getString(0).isEmpty()) {
				throw new Exception("error_message");
			}
			
			String serviceName = webSocketServerRequest.s.getString(0);
			
			if (WebSocketServerServiceMapping.servicesMapping.get(serviceName) == null) {
				throw new Exception("error_message");
			}
			
			int serviceSize = webSocketServerRequest.s.size();
			String[] serviceNameDetail = StringUtils.split(serviceName, ".", 2);
			
			if (isWebSocketClient 
					&& serviceNameDetail[0].equals("notice")) {
				throw new Exception("error_message");
			}
			
			Class<?>[] paramClass = null;
			Object[] serviceParams = null;
			
			if (serviceSize > 1) {
				serviceParams = new Object[serviceSize - 1];
				
				for (int i = 0; i < serviceSize - 1; i++) {
					serviceParams[i] = webSocketServerRequest.s.get(i + 1);
				}
				
				for (Class<?>[] p : WebSocketServerServiceMapping.servicesMapping.get(webSocketServerRequest.s.getString(0))) {
					if (p.length != serviceSize - 1) {
						continue;
					}

					int index = 1;
					for (Class<?> c : p) {
						if (c.getName() != webSocketServerRequest.s.get(index).getClass().getName()) {
							index = 0;
							break;
						}

						index++;
					}

					if (index != 0) {
						paramClass = p;
						break;
					}
				}
				
				if (paramClass == null) {
					logger.error("not found service method type mapping {}", webSocketServerRequest.s.toString());
					throw new Exception("error_message");
				}
			}
			
			Class<?> cls = Class.forName(String.format("%s.%sService", WebSocketServerServiceMapping.serviceNamespace, StringUtils.capitalize(serviceNameDetail[0])));
			Object obj = cls.newInstance();
			
			webSocketServerResponse.i = webSocketServerRequest.i;
			
			((WebSocketServerService) obj).isWebSocketClient = isWebSocketClient;
			((WebSocketServerService) obj).timestamp = WebUtil.time();
			((WebSocketServerService) obj).ip = ((InetSocketAddress)channel.remoteAddress()).getAddress().getHostAddress();
			((WebSocketServerService) obj).channel = channel;
			
			Method method = cls.getDeclaredMethod(serviceNameDetail[1], paramClass);
			webSocketServerResponse.d = method.invoke(obj, serviceParams);
		} catch (WebSokcetServerServiceException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			webSocketServerResponse.c = 1;
			webSocketServerResponse.m = e.getMessage();
			webSocketServerResponse.d = "";
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			doFinally = false;
			channel.close();
		} finally {
			if (isWebSocketClient && doFinally) {
				channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(webSocketServerResponse)));
			}
			
			logger.info("service handle {} {} {}", channel.remoteAddress(), webSocketServerRequest == null ? "" : JSON.toJSONString(webSocketServerRequest), JSON.toJSONString(webSocketServerResponse));
		}
	}

}
