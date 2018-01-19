package test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

public class TestOut {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(System.out), Charset.forName("utf-8"));
		PrintWriter pw = new PrintWriter(osw);
		pw.println("haha");
		pw.close();
	}

}
