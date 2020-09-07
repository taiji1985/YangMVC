package org.docshare.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import org.docshare.log.Log;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class IOUtil {
	public static String readStream(InputStream in){
		return readStream(in,"utf-8");
	}
	public static String readStream(InputStream in,String charset){
//		Scanner sc = new Scanner(in,charset);
//		StringBuffer sb =new StringBuffer();
//		while(sc.hasNextLine()){
//			sb.append(sc.nextLine());
//			sb.append('\n');
//		}
        try {
			byte[] buffer = new byte[1024];
			int len;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while((len = in.read(buffer)) != -1) {
			    bos.write(buffer, 0, len);
			}
			bos.close();
			return new String(bos.toByteArray(),charset);
		} catch (Exception e) {
			Log.e(e);
			return "";
		}
		
	}
}
