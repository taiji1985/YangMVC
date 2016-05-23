package org.docshare.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		/*
		 * if(bs == null || es == null || str == null) return "";
		 * 
		 * try{ String[] ret = getBetween(str,bs,es); if(ret == null)return
		 * null; return ret[0]; }catch(Exception e) { e.printStackTrace();
		 * return null; }
		 */
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
		String ret = "";
		if (str == null) {
			return "";
		}
		for (String s : str) {
			if (ret.length() > 0) {
				ret += join_str;
			}
			ret += s;
		}

		return ret;
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
}
