package org.docshare.boot;
import org.docshare.mvc.TextTool;

public class MIME {
	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	public static String getMIMEType(String fname) {
		String type = "*/*";
		
		String end = TextTool.getAfter(fname, ".");
		if (end == "")
			return type;
		// 在MIME和文件类型的匹配表中找到对应的MIME类型。
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	private static final String[][] MIME_MapTable = {
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
			{"json","application/json"}
			
	};
}
