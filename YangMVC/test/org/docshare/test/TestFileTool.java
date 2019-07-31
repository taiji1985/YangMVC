package org.docshare.test;

import org.docshare.util.FileTool;

import junit.framework.TestCase;

public class TestFileTool extends TestCase {

	public void testAppend(){
		String s = "吃了吗？";
		if(FileTool.exists("test.txt")){
			FileTool.delFile("test.txt");
		}
		FileTool.appendFile("test.txt", s,"utf-8");
		String r = FileTool.readAll("test.txt", "utf-8");
		System.out.println("r = "+r);
		System.out.println("s.length "+s.length()+" r len"+r.length());
		assertEquals(r, s);
	}
}
