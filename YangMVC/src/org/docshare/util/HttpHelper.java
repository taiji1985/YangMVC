package org.docshare.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

public class HttpHelper {
	/**
	 * 执行get请求
	 * @param url
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String get(String url) {
		GetMethod method = new GetMethod(url);

		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(10000);// 设置连接时间

		String response=null;
		try {
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				InputStream inputStream = method.getResponseBodyAsStream();
				response = IOUtil.readStream(inputStream);
			} else {
				response = null;
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	/**
	 * 执行post请求
	 * @param postUrl
	 * @param params
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String post(String postUrl, Map<String, Object> params) {
		String response = "";
		PostMethod postMethod = new PostMethod(postUrl);
		if (params != null) {
			for (String key : params.keySet()) {
				postMethod.addParameter(key, params.get(key).toString());
			}
		}
		try {
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(10000);// 设置连接时间
			int status = client.executeMethod(postMethod);
			if (status == HttpStatus.SC_OK) {
				InputStream inputStream = postMethod.getResponseBodyAsStream();
				response = IOUtil.readStream(inputStream);
			} else {
				response = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 释放连接
			postMethod.releaseConnection();
		}
		return response;
	}

//	public static void main(String[] args) {
//		HashMap<String, Object> map = new HashMap<String, Object>();
//		map.put("a", "12");
//		String ret = HttpHelper.post("http://localhost:8080/", map);
//		System.out.println(ret);
//		
//		ret = HttpHelper.get("http://localhost:8080/");
//		System.out.println(ret);
//	}
}
