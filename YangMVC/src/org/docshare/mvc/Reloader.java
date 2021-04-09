package org.docshare.mvc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.util.FileTool;

/**
 * 重新加载修改的class文件
 * @author Tongfeng Yang
 *
 */
class Reloader {
	private String root;
	HashMap<String, Long > last_tm=new HashMap<String, Long>();
	ArrayList<String> reloadList = new ArrayList<String>();
	ClassLoader loader = null;
	Class<?> clz = null;
	private String reloadPkg; //需要监测重新加载的包
	public Reloader(String clsRoot,String reloadPkg) {
		this.root = clsRoot;
		this.reloadPkg = reloadPkg;
	}
	int loaderVersion = 0;
	
	
	/**
	 * 递归扫描目录
	 * @param reloadDir
	 */
	public void scan(String reloadDir){
		T("start scan ...");
		if(reloadDir == null){
			reloadDir = root;
		}
		reloadList.clear();
		scan(new File(reloadDir));
		if(reloadList.size()>0){
			loader = new YangClassLoader(++loaderVersion,root,reloadPkg);
		}
		for(String p: reloadList){
			String cname = path2Class(p);
			reloadCls(p, cname);
		}
	}

	private void scan(File reloadF){
		File[] childs = reloadF.listFiles();
		for(File c: childs){
			//out.println(c.getAbsolutePath());
			if(c.isFile() && c.getName().endsWith(".class")){
				checkFile(c);
			}else{
				scan(c);
			}
		}
	}
	
	/**
	 * 路径转类名
	 * @param path
	 * @return
	 */
	private String path2Class(String path){
		String t = path.replace(root, "");
		t = t.replace("/", ".").replace("\\", ".").replace(".class", "");
		if(t.startsWith(".")){
			t = t.substring(1);
		}
		return t;
		
	}
	private void checkFile(File c) {
		String path = c.getAbsolutePath();
		long now = c.lastModified();
		if(last_tm.containsKey(path)){
			long last = last_tm.get(path);
			if(now > last){
				reloadList.add(path);
				T("find a file need to reload "+path);
				last_tm.put(path, now);
			}
		}else{
			last_tm.put(path, now);
		}
	}
	public Class<?> load(String clsName) throws ClassNotFoundException{
		if(!Config.reloadable){
			Class<?> ret = Class.forName(clsName);
			MethodAccessCacher.putIfNoExist(clsName,ret);
		}
		//下面是打开了reloadable选项后的功能
		
		ClassLoader loader  = classIsUpdate(clsName);
		return Class.forName(clsName, true, loader);
	}
	

	
	/**
	 * 检查是否更新了，如果更新了。返回真，并记录这一次的文件修改时间
	 * @param path
	 * @return
	 */
	public ClassLoader classIsUpdate(String clsName){
		String path = clsToPath(clsName);
		if(! FileTool.exists(path) && FileTool.exists("bin/") && ! path.contains("bin/") ){
			path = "bin"+path;
		}

		File c = new File(path);
		long now = c.lastModified();
		if(last_tm.containsKey(path)){
			long last = last_tm.get(path);
			if(now > last){
				
				T("find a file need to reload "+path);
				last_tm.put(path, now);
				loader = new YangClassLoader(++loaderVersion,root,reloadPkg);
				return loader;
			}
		}else{
			last_tm.put(path, now);
			
		}
		if(loader == null){
			loader = new YangClassLoader(++loaderVersion,root,reloadPkg);
		}
		return loader;
	}
	
	private String clsToPath(String clsName) {
		if(!root.endsWith("/"))root += "/";
		
		return root+clsName.replace(".","/") +".class";
	}

	private void reloadCls(String fileName,String clsName) {
		try {
				T("reload "+clsName +" , from "+fileName);
				clz = loader.loadClass(clsName);
				T("load succ "+ clz.hashCode());
				//Animal animal = (Animal)clz.newInstance();
				//animal.speak();
			
		} catch (ClassNotFoundException e) {
			T("person class not found");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void T(String s) {
		System.out.println(s);
	}
	
	public static void main(String[] args) throws InterruptedException {
		Reloader reload = new Reloader("bin","org");
		while(true){
			reload.scan("bin");
			Thread.sleep(1000);
		}

	}
	public static void startThread(String reloadDir){
		Thread t =new Thread(new Runnable() {
			
			@Override
			public void run() {
				Reloader reload = new Reloader("bin","org");
				while(true){
					try {
						reload.scan("bin");
						
					} catch (Exception e) {
						e.printStackTrace();
						Log.i("Reloader Thread Exception ,restart 30 second later ");
						try {
							Thread.sleep(30*1000);
						} catch (InterruptedException e1) {
							System.out.println("Reload thread interupted");
							return;
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						System.out.println("Reload thread interupted");
						return;
					}
				}
			}
		});
		t.start();
	}
	
	@SuppressWarnings("unused")
	private static long getFileLastTm(String path){
		File f = new File(path);
		if(!f.exists()){
			return -1;
		}
		return f.lastModified();
	}
}
