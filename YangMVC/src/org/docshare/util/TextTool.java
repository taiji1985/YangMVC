package org.docshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.docshare.log.Log;
import org.docshare.mvc.Config;

public class TextTool {

	public static byte[] readAllBytes(String fname) {
		File f = new File(fname);
		if (!f.exists())
			return null;
		long fileSize = f.length();
		if (fileSize > Integer.MAX_VALUE) {
			System.out.println("file too big...");
			return null;
		}
		byte[] buffer = null;
		FileInputStream fi;
		try {
			buffer = new byte[(int) fileSize];
			fi = new FileInputStream(f);
			int offset = 0;
			int numRead = 0;
			while (offset < buffer.length
					&& (numRead = fi.read(buffer, offset, buffer.length
							- offset)) >= 0) {
				offset += numRead;
			}

			fi.close();
		} catch (FileNotFoundException e) {
			Log.e(e);
		} catch (IOException e) {
			Log.e(e);
		}

		return buffer;

	}

	public static String removeCRLF(String s) {
		s = s.replace("\r", "");
		s = s.replace("\n", "");
		return s;
	}

	public static int getTextCount(String content, String subs) {
		int i = 0;
		int c = 0;
		while (true) {
			i = content.indexOf(subs, i + subs.length() + 1);
			if (i < 0)
				break;

			c++;
		}

		return c;
	}

	public static String getXmlItem(String str, String itemname) {
		String ret = getBetweenOne(str, "<" + itemname + ">", "</" + itemname
				+ ">");
		if (ret == null)
			return "";

		ret = ret.replace("<![CDATA[", "");
		ret = ret.replace("]]>", "");

		return ret;
	}

	public static String getBetweenOne(String str, String bs, String es) {
		
		if (bs == null || es == null || str == null)
			return null;
		int b = -1, e = 0;

		b = str.indexOf(bs, b + 1);
		if (b < 0)
			return null;
		e = str.indexOf(es, b + bs.length());
		if (e < 0)
			return null;

		return str.substring(b + bs.length(), e);

	}

	public static String[] getBetween(String str, String bs, String es) {
		if (bs == null || es == null || str == null)
			return null;
		ArrayList<String> al = new ArrayList<String>();
		int b = -1, e = 0;

		while (true) {
			b = str.indexOf(bs, b + 1);
			if (b < 0)
				break;
			e = str.indexOf(es, b + bs.length());
			if (e < 0)
				break;

			al.add(str.substring(b + bs.length(), e));
		}
		if (al.size() == 0) {
			return null;
		}
		String[] a = new String[al.size()];
		return al.toArray(a);
	}

	public static String join(String[] str, String join_str) {
		if (str == null) {
			return "";
		}
		StringBuilder sb =new StringBuilder();
		for (String s : str) {
			if (sb.length() > 0) {
				sb.append(join_str);
			}
			sb.append(s);
		}

		return sb.toString();
	}

	public static String getBefore(String str, String mark) {
		int i = str.indexOf(mark);
		if (i < 0)
			return null;

		return str.substring(0, i);
	}

	public static String getBeforeLast(String str, String mark) {
		int i = str.lastIndexOf(mark);
		if (i <= 0)
			return null;

		return str.substring(0, i);
	}

	public static String getAfter(String str, String mark) {
		int i = str.indexOf(mark);
		if (i <= 0)
			return null;

		return str.substring(i + mark.length());
	}
	public static String getLastAfter(String str, String mark) {
		int i = str.lastIndexOf(mark);
		if (i <= 0)
			return null;

		return str.substring(i + mark.length());
	}
	public static String getPrefix(String f) {
		int p = f.lastIndexOf(".");
		if (p >= 0)
			return f.substring(p + 1);

		return null;
	}

	public static String getBetweenTwo(String d, String s1, String e1,
			String s2, String e2) {
		d = TextTool.getBetweenOne(d, s1, e1);
		if (d == null)
			return null;

		d = TextTool.getBetweenOne(d, s2, e2);
		return d;

	}
	public static String firstUpper(String s){
		if(s == null)return null;
		
		char[] arr = s.toCharArray();
		if(arr.length>0 && arr[0]>='a'&&arr[0]<='z'){
			arr[0] = (char) (arr[0] - 'a'+'A');
		}
		return new String(arr);
		
	}

	public static String firstLower(String s) {
		if(s == null)return null;
		
		char[] arr = s.toCharArray();
		if(arr.length>0 && arr[0]>='A'&&arr[0]<='Z'){
			arr[0] = (char) (arr[0] - 'A'+'a');
		}
		return new String(arr);
	}

	
	public static String join2(List<String> str, String join_str) {
		if (str == null ||str.size() == 0) {
			return "";
		}
		StringBuilder ret =new StringBuilder();
		for (String s : str) {
			if (ret.length() > 0) {
				ret.append(join_str);
			}
			ret.append(s);
		}

		return ret.toString();
	}

	public static String join(Object[] str, String join_str) {
		
		if (str == null) {
			return "";
		}
		StringBuilder ret =new StringBuilder();
		for (Object s : str) {
			if (ret.length() > 0) {
				ret.append(join_str);
			}
			ret.append(s.toString());
		}

		return ret.toString();
	}

	public static String join(List<Object> params, String join_str) {
		if(params == null) return null;
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<params.size()-1;i++){
			sb.append(""+params.get(i));
			sb.append(",");
			
		}
		if(params.size()>=1){
			sb.append(params.get(params.size()-1));
		}
		return sb.toString();
	}
	public static String getParentPackage(String a){
		if(a == null)return null;
		int dotp = a.lastIndexOf(".");
		if(dotp <  0) return a;
		return a.substring(0,dotp);
	}
	public static StringBuilder concat(Object ...args){
		StringBuilder sBuffer =new StringBuilder();
		for(Object s:args){
			sBuffer.append(s);
		}
		return sBuffer;		
	}
	/**
	 * 下划线式的分割转 驼峰式   如： aa_bb 转 aaBb
	 * @param s 原字符串
	 * @return  目标字符串
	 */
	public static String underLineToUpper(String s){
		char[] ca = s.toCharArray();
		int j = 0;
		int i;
		for(i=0;i<ca.length-1;i++){
			if(ca[i] == '_'){
				ca[i+1] = Character.toUpperCase(ca[i+1]);
				continue;
			}
			ca[j] = ca[i];
			j ++;
		}
		ca[j] = ca[i];
		return new String(ca, 0, ++j);
	}
	
	public static String txt2HTML(String msg){
		StringBuilder sBuffer = new StringBuilder();
		sBuffer.append("<html><head><meta charset='utf-8'/></head><body>");
		String m =msg.replace("\n", "\n<br>").replace("\t","&nbsp;&nbsp;&nbsp;&nbsp;");
		m = m.replace(Config.controller, "<font color='red'>"+Config.controller+"</font>");
		sBuffer.append(m);
		sBuffer.append("</body></html>");
		return sBuffer.toString();
	}
}
