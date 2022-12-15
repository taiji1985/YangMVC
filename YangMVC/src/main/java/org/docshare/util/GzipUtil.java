package org.docshare.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GzipUtil {

	public static boolean isGzipSupported(HttpServletRequest request) {
		String encodings = request.getHeader("Accept-Encoding");
		return ((encodings != null) && (encodings.indexOf("gzip") != -1));
	}

	public static boolean isGzipDisabled(HttpServletRequest request) {
		String flag = request.getParameter("disableGzip");
		return ((flag != null) && (!flag.equalsIgnoreCase("false")));
	}

	public static PrintWriter getGzipWriter(HttpServletResponse response)
			throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(response.getOutputStream()), Charset.forName("utf-8"));
		return new PrintWriter(osw);
		//return new PrintWriter(	new GZIPOutputStream(response.getOutputStream()));
	}
}
