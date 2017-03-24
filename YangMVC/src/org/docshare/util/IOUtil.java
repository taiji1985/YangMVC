package org.docshare.util;

import java.io.InputStream;
import java.util.Scanner;

public class IOUtil {
	public static String readStream(InputStream in){
		Scanner sc = new Scanner(in,"utf-8");
		StringBuffer sb =new StringBuffer();
		while(sc.hasNextLine()){
			sb.append(sc.nextLine()+"\n");
		}
		
		return sb.toString();
		
	}
}
