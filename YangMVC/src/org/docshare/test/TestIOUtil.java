package org.docshare.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import org.docshare.util.IOUtil;

public class TestIOUtil  extends TestCase {
	public void testRead() throws FileNotFoundException{
		FileInputStream fi=new FileInputStream("MANIFEST.MF");
		String s= IOUtil.readStream(fi);
		System.out.println(s);
	}
}
