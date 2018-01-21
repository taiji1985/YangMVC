package org.docshare.boot;
import org.docshare.mvc.TextTool;

public class MIME {
	/**
	 * �����ļ���׺����ö�Ӧ��MIME���͡�
	 * 
	 * @param file
	 */
	public static String getMIMEType(String fname) {
		String type = "*/*";
		
		String end = TextTool.getAfter(fname, ".");
		if (end == "")
			return type;
		// ��MIME���ļ����͵�ƥ������ҵ���Ӧ��MIME���͡�
		for (int i = 0; i < MIME_MapTable.length; i++) {
			if (end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}

	private static final String[][] MIME_MapTable = {
			// {��׺���� MIME����}
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
