package org.docshare.yangmvc.boot;
import java.util.HashMap;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.util.FileTool;
import org.docshare.yangmvc.util.TextTool;

public class MIME {
	private static final Logger logger = LoggerFactory.getLogger(MIME.class);

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
		logger.debug("MIME load "+map.size()+" items");
	}
	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * 
	 * @param file
	 */
	public static String getMIMEType(String fname) {
		logger.trace("MIME "+fname);
		String ret =  map.get(TextTool.getPrefix(fname));
		return ret == null?"text/html":ret;
	}

}
