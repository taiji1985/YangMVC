package org.docshare.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.docshare.log.Log;

public class HttpHelper {
	/**
	 * 执行get请求
	 * @param url 网页路径
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String get(String url) {
		return get(url,"utf-8");
	}
	static HttpClient client = new HttpClient();
	public static String get(String url,String charset) {
		GetMethod method = new GetMethod(url);

		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(10000);// 设置连接时间

		String response=null;
		try {
			int status = client.executeMethod(method);
			if (status == HttpStatus.SC_OK) {
				InputStream inputStream = method.getResponseBodyAsStream();
				response = IOUtil.readStream(inputStream,charset);
			} else {
				response = null;
			}
		} catch (HttpException e) {
			Log.e(e);
		} catch (IOException e) {
			Log.e(e);
		}
		return response;
	}
	/**
	 * 执行post请求，返回页面内容
	 * @param postUrl 网址
	 * @param params  参数
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String post(String postUrl, Map<String, Object> params) {
		return post(postUrl,params,"utf-8");
	}
	/**
	 * 执行post请求，返回页面内容
	 * @param postUrl 网址
	 * @param params  参数
	 * @param charset 编码方式
	 * @return 如果失败返回null，否则返回内容
	 */
	public static String post(String postUrl, Map<String, Object> params,String charset) {
		String response = "";
		PostMethod postMethod = new PostMethod(postUrl);
		if (params != null) {
			for (String key : params.keySet()) {
				postMethod.addParameter(key, params.get(key).toString());
			}
		}
		try {
			client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, charset);
			client.getHttpConnectionManager().getParams()
					.setConnectionTimeout(10000);// 设置连接时间
			int status = client.executeMethod(postMethod);
			
			InputStream inputStream = postMethod.getResponseBodyAsStream();
			response = IOUtil.readStream(inputStream,charset);
			if((""+status).startsWith("5")){
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
