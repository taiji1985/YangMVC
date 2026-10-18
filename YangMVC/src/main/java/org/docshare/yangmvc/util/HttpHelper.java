package org.docshare.yangmvc.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的 HTTP 工具（GET / 表单 POST）。
 *
 * <p>历史实现用的是 commons-httpclient 3.1（2011 年 EOL，CVE-2012-5783 无修复），
 * 这里换成 Apache HttpClient 4.5（Java 8 兼容），对外方法签名保持不变：
 * {@link #get(String)}、{@link #get(String, String)}、{@link #post(String, Map)}、
 * {@link #post(String, Map, String)}。
 */
public class HttpHelper {
	private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);

	private static final CloseableHttpClient CLIENT = HttpClients.custom()
			.setDefaultRequestConfig(RequestConfig.custom()
					.setConnectTimeout(10000)
					.setConnectionRequestTimeout(10000)
					.setSocketTimeout(10000)
					.build())
			.build();

	/**
	 * 执行get请求
	 * @param url 网页路径
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String get(String url) {
		return get(url, "utf-8");
	}

	public static String get(String url, String charset) {
		HttpGet method = new HttpGet(url);
		try (CloseableHttpResponse resp = CLIENT.execute(method)) {
			int status = resp.getStatusLine().getStatusCode();
			if (status != 200) {
				return null;
			}
			HttpEntity entity = resp.getEntity();
			return entity == null ? "" : EntityUtils.toString(entity, charset);
		} catch (IOException e) {
			logger.error("", e);
			return null;
		}
	}

	/**
	 * 执行post请求，返回页面内容
	 * @param postUrl 网址
	 * @param params  参数
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String post(String postUrl, Map<String, Object> params) {
		return post(postUrl, params, "utf-8");
	}

	/**
	 * 执行post请求，返回页面内容
	 * @param postUrl 网址
	 * @param params  参数
	 * @param charset 编码方式
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String post(String postUrl, Map<String, Object> params, String charset) {
		HttpPost method = new HttpPost(postUrl);
		String response = "";
		try {
			if (params != null) {
				List<NameValuePair> form = new ArrayList<NameValuePair>(params.size());
				for (Map.Entry<String, Object> e : params.entrySet()) {
					Object v = e.getValue();
					form.add(new BasicNameValuePair(e.getKey(), v == null ? "" : v.toString()));
				}
				method.setEntity(new UrlEncodedFormEntity(form, charset));
			}
			try (CloseableHttpResponse resp = CLIENT.execute(method)) {
				int status = resp.getStatusLine().getStatusCode();
				HttpEntity entity = resp.getEntity();
				response = entity == null ? "" : EntityUtils.toString(entity, charset);
				if (String.valueOf(status).startsWith("5")) {
					return null;
				}
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		return response;
	}
}
