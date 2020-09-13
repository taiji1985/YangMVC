package org.docshare.mvc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.docshare.log.Log;
import org.docshare.util.IOUtil;

import com.sun.corba.se.spi.orbutil.fsm.Input;


public class Rewriter {
	private String root;

	public Rewriter(String root){
		load(root);
	}
	public InputStream tryGetInputStream(String filepath){
		//先尝试当前目录
		File f = new File(filepath);
		if(f.exists()){
			try {
				Log.d("find ",filepath," at ",f.getAbsolutePath());
				return new FileInputStream(f);
			} catch (FileNotFoundException e) {
			}
		}
		//再尝试web目录
		f=new File(root+filepath);
		if(f.exists()){
			try {
				Log.d("find ",filepath," at ",f.getAbsolutePath());
				return new FileInputStream(f);
			} catch (FileNotFoundException e) {
			}
		}
		//在尝试classpath
		InputStream ret= getClass().getResourceAsStream(filepath);
		if(ret!=null){
			Log.d("find ",filepath," at classpath");
		}
		return ret;
	}	
	static class RewritePair{
		@Override
		public String toString() {
			return "RewritePair [pattern=" + pattern + ", replace=" + replace + "]";
		}
		public RewritePair(Pattern pattern2, String to) {
			this.pattern =pattern2;
			this.replace = to;
		}
		
		Pattern pattern;
		String replace;
	}
	RewritePair[] pairs=null;
	private void load(String root) {
		this.root =root;
		InputStream in = tryGetInputStream("/rewrite.txt");
		if(in == null){
			Log.i("can not find /rewrite.txt");
			return;
		}
		String string = IOUtil.readStream(in, "utf-8");
		String[] sa = string.split("\n");
		ArrayList<RewritePair> pa=new ArrayList<>();
		for(String line : sa){
			line=line.trim();
			if(line.length()>0){
				int p = line.indexOf(' ');
				if(p<=0)continue;
				String from = line.substring(0,p).trim();
				Pattern pattern=Pattern.compile(from);
				String to = line.substring(p+1).trim();
				pa.add(new RewritePair(pattern,to));
			}
		}
		pairs = pa.toArray(new RewritePair[pa.size()]);
	}
	/**
	 * 规则匹配，重写url
	 * @param uri
	 * @return
	 */
	public String rewrite(String uri){
		if(pairs==null)return uri; //do nothing if no rewrite rule
		for(RewritePair p:pairs){
			Matcher m = p.pattern.matcher(uri);
			String n=m.replaceFirst(p.replace);
			if(!n.equals(uri)){
				Log.d("Rewrite ",uri," to ",n);
				return n;
			}
		}
		return uri;
	}
}
