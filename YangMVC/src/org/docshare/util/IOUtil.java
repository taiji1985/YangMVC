package org.docshare.util;

import java.io.InputStream;
import java.util.Scanner;

public class IOUtil {
	public static String readStream(InputStream in){
		return readStream(in,"utf-8");
	}
	public static String readStream(InputStream in,String charset){
		Scanner sc = new Scanner(in,charset);
		StringBuffer sb =new StringBuffer();
		while(sc.hasNextLine()){
			sb.append(sc.nextLine()+"\n");
		}
		
		return sb.toString();
		
	}
}
