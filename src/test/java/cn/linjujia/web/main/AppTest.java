package cn.linjujia.web.main;

import org.junit.Test;

import cn.linjujia.web.core.WebHttp;
import cn.linjujia.web.core.WebConfig;
import cn.linjujia.web.core.WebUtil;

/**
 * 
 */
public class AppTest {
	
	@Test
	public void testNotice() {
		WebHttp webHttp = WebHttp.getInstance();
		
		WebConfig.bootstrap();
		
		String params = "{\"s\":[\"player.notice\",\"123-456\", \"tet\"], \"i\":1}";
		String token = WebUtil.md5(String.format("%s%s", params, WebConfig.tokenKey).getBytes());
		
		String response = webHttp.post("http://localhost:8080/?token=" + token, params);
		System.out.println(response);
	}
}
