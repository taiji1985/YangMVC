package org.docshare.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.docshare.log.Log;

public class FileTool {

	


	/**
	 * 读取文件中所有的字节
	 * @param filename 文件名
	 * @return 读取的字节数组
	 */

	public static byte[] readBytes(String filename) {
		byte[] ret = new byte[1024 * 10009]; // 200k
		int p = 0;
		try {
			FileInputStream fin = new FileInputStream(filename);
			byte[] b = new byte[1024 * 1000];
			while (true) {
				int n = fin.read(b);
				if (n <= 0)
					break;
				for (int i = 0; i < n; i++) {
					ret[p++] = b[i];
				}

			}
			byte[] newr = new byte[p];
			for (int i = 0; i < p; i++) {
				newr[i] = ret[i];
			}

			return newr;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}
	/**
	 * 读取文件中所有的内容，编码方式为GB2312
	 * 该函数已过期
	 * @param f 文件名
	 * @return 文件内容
	 */
	@Deprecated
	public static String readAll(String f) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "GB2312"));
			while (br.ready()) {
				sb.append(br.readLine() + "\n");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}

		return sb.toString();
	}
	/**
	 * 读取文件中所有的内容，编码方式为GB2312 ,行为补充\r\n
	 * 该函数已过期
	 * @param f 文件名
	 * @return 文件内容
	 */
	public static String readAllCRLF(String f) {
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(f), "GB2312"));
			while (br.ready()) {
				sb.append(br.readLine() + "\r\n");
			}
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}

		return sb.toString();
	}
	/**
	 * 读取文件内容，给定编码方式
	 * @param f 文件名或路径
	 * @param charset 文件编码方式
	 * @return 文件内容
	 */
	public static String readAll(String f, String charset) {
		if (!FileTool.exists(f))
			return null;
		BufferedReader br = null;
		try {
			return readAll(new FileInputStream(f),charset);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * 根据输入流进行读取。
	 * @param in
	 * @param charset
	 * @return 文件内容
	 */
	public static String readAll(InputStream in, String charset) {
		if (in == null)
			return null;

		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		try {
			InputStreamReader reader = new InputStreamReader(in, charset);
			br = new BufferedReader(reader);
			while (br.ready()) {
				sb.append(br.readLine() + "\n");
			}
			reader.close();
		} catch (Exception e) {
			Log.e(e);
			e.printStackTrace();
			return null;
		}

		return sb.toString();
	}

	@Deprecated
	public static void writeUTFOLD(String f, String data) {
		try {
			DataOutputStream ds = new DataOutputStream(new FileOutputStream(f));
			data = new String(data.getBytes(), "UTF-8");
			ds.writeUTF(data);
			ds.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 以utf-8编码进行写文件
	 * @param f 文件名
	 * @param data 待写入的内容
	 */
	public static void writeUTF(String f, String data) {
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
			out.write(data);
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 将所有内容写入文件中
	 * @param f 文件路径
	 * @param data 待写入内容
	 * @param charset 编码方式
	 */
	public static void writeAll(String f, String data, String charset) {
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(new FileOutputStream(f), charset);
			out.write(data);
			out.flush();
			out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Deprecated
	public static void writeAll(String f, String data) {

		PrintWriter pw;
		try {

			pw = new PrintWriter(f);
			pw.print(data);
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取父文件夹
	 * @param d
	 * @return
	 */
	public static String getParentDir(String d) {
		d = d.replace('\\', '/');
		int i = d.lastIndexOf('\\');
		if (i <= 0) {
			return null;
		}

		d = d.substring(0, i);
		return d;
	}
	/**
	 * 创建文件夹
	 * @param d
	 */
	public static void makeDir(String d) {
		File f = new File(d);
		if (f.exists())
			return;
		f.mkdirs();
	}

	/**
	 * 在文件中追加
	 * @param f 文件名
	 * @param d 内容
	 */
	public static void appendFile(String f, String d) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new FileWriter(f, true));
			pw.print(d);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 删除文件
	 * @param f 文件名
	 */
	public static void delFile(String f) {
		try {
			File file = new File(f);
			file.delete();
		} catch (Exception e) {

		}
	}
	/**
	 * 判断文件是否存在
	 * @param filename
	 * @return
	 */
	public static boolean exists(String filename) {
		File f = new File(filename);
		return f.exists();
	}

	public static ArrayList<Double> loadBinMatrix(String file) {
		try {
			ArrayList<Double> al = new ArrayList<Double>();
			DataInputStream di = new DataInputStream(new FileInputStream(file));
			while (di.available() >= 8) {
				double d = di.readDouble();
				al.add(d);
			}
			di.close();
			return al;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<Double> loadTextMatrix(String filename) {

		ArrayList<Double> al = new ArrayList<Double>();
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Scanner sc = new Scanner(fr);
		while (sc.hasNextDouble()) {
			double num = sc.nextDouble();
			al.add(num);
			if (al.size() % 10000 == 0) {
				System.out.println("load m " + al.size());
			}
		}

		System.out.println("al= " + al.size() + "\n");
		return al;
	}

	public static void saveBinMatrix(String file, ArrayList<?> al) {
		try {
			DataOutputStream dp = new DataOutputStream(new FileOutputStream(
					file));
			for (int i = 0; i < al.size(); i++) {
				double d = (Double) al.get(i);
				dp.writeDouble(d);
			}
			dp.close();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	public static long lastModify(String fn) {
		File f = new File(fn);

		return f.lastModified();
	}

	public static void printHashMap(HashMap<?, ?> hm) {
		Iterator<?> iterator = hm.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();

			Integer value = (Integer) hm.get(key);
			System.out.println(key + "=" + value);
		}
	}

	public static void touch(String f) {
		FileTool.writeAll(f, "");
	}

	public static void delPath(String filepath) {
		if (!FileTool.exists(filepath))
			return;

		File f = new File(filepath);
		File[] sfl = f.listFiles();
		for (File sf : sfl) {
			if (sf.isFile()) {
				sf.delete();
				continue;
			}
			if (sf.isDirectory()) {
				delPath(sf.getPath());
			}
		}
		f.delete();
	}

	public static String[] readLines(String filename, String charset) {
		return FileTool.readAll(filename, charset).split("\n");
	}

	/**
	 * 读取资源
	 * 
	 * @param fname
	 *            相对根目录的文件名
	 * @param charset
	 *            文件编码方式
	 * @return 文件内容，如果没读到，返回null
	 */
	public static String readResource(String fname, String charset) {
		try {
			InputStream sm = FileTool.class.getClassLoader()
					.getResourceAsStream(fname);
			Scanner sc = new Scanner(sm);
			StringBuffer sb = new StringBuffer();
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeAll(OutputStream os, String data, String charset) {
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(os, charset);
			out.write(data);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeAll(InputStream in, OutputStream os) {
		byte[] buf = new byte[1024];

		try {
			int readed = in.read(buf);
			while (readed > 0) {
				os.write(buf, 0, readed);
				readed = in.read(buf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
