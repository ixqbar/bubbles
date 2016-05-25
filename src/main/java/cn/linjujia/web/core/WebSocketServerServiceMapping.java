package cn.linjujia.web.core;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebSocketServerServiceMapping {
	
	public static Map<String, List<Class<?>[]>> servicesMapping = new LinkedHashMap<>();
	public static String serviceNamespace = "cn.linjujia.web.service";

	static {
		servicesMapping.put("player.login", Arrays.asList(new Class[] { String.class, String.class }, null));
		servicesMapping.put("notice.push", Arrays.asList(new Class[] { String.class, String.class }, null));
		servicesMapping.put("notice.pushAll", Arrays.asList(new Class[] { String.class }, null));
	}
}
