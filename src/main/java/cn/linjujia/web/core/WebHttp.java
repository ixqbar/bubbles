package cn.linjujia.web.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class WebHttp {

	private final static Logger logger = LoggerFactory.getLogger(WebHttp.class);
	
	private final static String USER_AGENT = "JZGAMEHTTPUTIL";
	
	public static WebHttp getInstance() {
		return new WebHttp();
	}
	
	private HttpClient httpClient;
	
	public WebHttp() {
		httpClient = HttpClientBuilder.create().build();
	}
	
	
	public String get(String url) {
		String result = "";
		try {
			HttpGet request = new HttpGet(url);
			request.addHeader("User-Agent", USER_AGENT);
			HttpResponse response = httpClient.execute(request);
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("get|{}|{}", url, result);
		}
		
		return result;
	}
	
	
	public String post(String url, Map<String, String> params) {
		String result = "";
		try {
			HttpPost request = new HttpPost(url);
			List<NameValuePair> parameters = new ArrayList<>();
			
			for(String name : params.keySet()) {
				parameters.add(new BasicNameValuePair(name, params.get(name)));
			}
			
			request.setEntity(new UrlEncodedFormEntity(parameters));
			HttpResponse response = httpClient.execute(request);
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("post|{}|{}|{}", url, JSONObject.toJSONString(params), result);
		}
		
		return result;
	}
	
	public String post(String url, String params) {
		String result = "";
		try {
			HttpPost request = new HttpPost(url);
			request.setEntity(new StringEntity(params));
			HttpResponse response = httpClient.execute(request);
			result = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logger.info("post|{}|{}|{}", url, JSONObject.toJSONString(params), result);
		}
		
		return result;
	}
	
}
