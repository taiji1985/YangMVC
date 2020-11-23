package org.docshare.boot;
import java.util.HashMap;
import java.util.Scanner;

import org.docshare.log.Log;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

public class MIME {
	static HashMap<String, String> map=new HashMap<String, String>();

	public static void start(){
		map.clear();

		Scanner scanner = new Scanner(MIME.class.getResourceAsStream("mime.txt"), "utf-8");
		while(scanner.hasNext()){
			String prefix = scanner.next();
			if( ! scanner.hasNext()) break;
			String mime = scanner.next();
			map.put(prefix.trim(), mime.trim());
		}
		FileTool.safelyClose(scanner);
		Log.d("MIME load "+map.size()+" items");
	}
	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	public static String getMIMEType(String fname) {
		Log.v("MIME "+fname);
		String ret =  map.get(TextTool.getPrefix(fname));
		return ret == null?"text/html":ret;
	}

}
