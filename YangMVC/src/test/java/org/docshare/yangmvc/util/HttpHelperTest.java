package org.docshare.yangmvc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * HttpHelper（从 commons-httpclient 3.1 换到 HttpClient 4.5）的回归测试。
 * 用 JDK 自带的 HttpServer 起一个本地端点，不依赖外网。
 */
public class HttpHelperTest {

	private static HttpServer server;
	private static String base;

	@BeforeClass
	public static void start() throws Exception {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/ok", new com.sun.net.httpserver.HttpHandler() {
			@Override
			public void handle(HttpExchange ex) throws java.io.IOException {
				byte[] b = "hello".getBytes("UTF-8");
				ex.sendResponseHeaders(200, b.length);
				OutputStream os = ex.getResponseBody();
				os.write(b);
				os.close();
			}
		});
		server.createContext("/echo", new com.sun.net.httpserver.HttpHandler() {
			@Override
			public void handle(HttpExchange ex) throws java.io.IOException {
				byte[] body = readAll(ex.getRequestBody());
				ex.sendResponseHeaders(200, body.length);
				OutputStream os = ex.getResponseBody();
				os.write(body);
				os.close();
			}
		});
		server.createContext("/err", new com.sun.net.httpserver.HttpHandler() {
			@Override
			public void handle(HttpExchange ex) throws java.io.IOException {
				ex.sendResponseHeaders(500, -1);
				ex.close();
			}
		});
		server.start();
		base = "http://127.0.0.1:" + server.getAddress().getPort();
	}

	@AfterClass
	public static void stop() {
		if (server != null) {
			server.stop(0);
		}
	}

	@Test
	public void getOk() {
		assertEquals("hello", HttpHelper.get(base + "/ok"));
	}

	@Test
	public void getNon200ReturnsNull() {
		assertNull(HttpHelper.get(base + "/err"));
	}

	@Test
	public void postEncodesForm() {
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		params.put("a", "1");
		params.put("b", "x y");
		String body = HttpHelper.post(base + "/echo", params);
		assertTrue("body=" + body, body.contains("a=1"));
		assertTrue("空格应编码为 +: " + body, body.contains("b=x+y"));
	}

	@Test
	public void post5xxReturnsNull() {
		assertNull(HttpHelper.post(base + "/err", new LinkedHashMap<String, Object>()));
	}

	private static byte[] readAll(InputStream in) throws java.io.IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int n;
		while ((n = in.read(buf)) > 0) {
			bos.write(buf, 0, n);
		}
		return bos.toByteArray();
	}
}
