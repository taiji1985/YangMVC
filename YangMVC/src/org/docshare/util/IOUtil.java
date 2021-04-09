package org.docshare.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.docshare.log.Log;

public class IOUtil {
	public static String readStream(InputStream in){
		return readStream(in,"utf-8");
	}
	public static String readStream(InputStream in,String charset){
//		Scanner sc = new Scanner(in,charset);
//		StringBuilder sb =new StringBuilder();
//		while(sc.hasNextLine()){
//			sb.append(sc.nextLine());
//			sb.append('\n');
//		}
		ByteArrayOutputStream bos=null;
        try {
			byte[] buffer = new byte[1024];
			int len;
			 bos = new ByteArrayOutputStream();
			while((len = in.read(buffer)) != -1) {
			    bos.write(buffer, 0, len);
			}
			return new String(bos.toByteArray(),charset);
		} catch (Exception e) {
			Log.e(e);
			return "";
		}finally {
			FileTool.safelyClose(bos);
		}
		
	}
}
