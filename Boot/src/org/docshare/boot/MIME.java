package org.docshare.boot;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.TextTool;

public class MIME {
	static HashMap<String, String> map=new HashMap<String, String>();
	public MIME(){
		
	}
	public static void start(){
		map.clear();
		for (int i = 0; i < table.length; i++) {
			map.put(table[i][0], table[i][1]);
		}
	}
	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	public static String getMIMEType(String fname) {
		Log.d("MIME"+fname);
		return map.get(TextTool.getPrefix(fname));
	}

	private static final String[][] table = {
			// {后缀名， MIME类型}
			{ "doc", "application/msword" },
			{ "docx",
					"application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
			{ "xls", "application/vnd.ms-excel" },
			{ "xlsx",
					"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
			{ "pdf", "application/pdf" },
			{ "ppt", "application/vnd.ms-powerpoint" },
			{ "pptx",
					"application/vnd.openxmlformats-officedocument.presentationml.presentation" },
			{ "txt", "text/plain" }, { ".wps", "application/vnd.ms-works" },
			{ "", "*/*" },
			{"html","text/html"},
			{"htm","text/html"},
			{"json","application/json"},
			{"png","image/png"},
			{"jpg","image/jpeg"},
			{"jpeg","image/jpeg"},
			{"bmp","application/x-MS-bmp"},
			{"ico","image/x-icon"},
			
	};
}
