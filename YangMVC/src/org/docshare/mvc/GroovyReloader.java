package org.docshare.mvc;

import java.io.File;
import java.io.IOException;

import org.codehaus.groovy.control.CompilationFailedException;
import org.docshare.log.Log;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

import groovy.lang.GroovyClassLoader;

/**
 * 支持groovy的reloader
 * @author lenovo
 *
 */
public class GroovyReloader extends Reloader {

	public GroovyReloader(){
		
	}
	public GroovyReloader(String clsRoot, String reloadPkg) {
		super(clsRoot, reloadPkg);
	}
	
	public Class<?> load(String clsName) throws ClassNotFoundException{
		if(!Config.reloadable){
			Class<?> ret = Class.forName(clsName);
			MethodAccessCacher.putIfNoExist(clsName,ret);
		}
		//下面是打开了reloadable选项后的功能
		
		ClassLoader loader  = classIsUpdate(clsName);
		
		//如果发现有groovy文件
		if(loader == groovyLoader){
			String path = clsToPath(clsName);
			
			String groovy_path = Config.getProperty("groovy", "groovy");
			File c = new File(groovy_path,path.replace(".class",".groovy")); //找groovy源代码
			if(c.exists()){
				try {
					
					return groovyLoader.parseClass(c);
				} catch (CompilationFailedException | IOException e) {
					Log.e(e);
				}
			}
			Log.d("file path "+ c.getAbsolutePath());
			
		}
		
		return Class.forName(clsName, true, loader);
	}
	GroovyClassLoader groovyLoader = null;
	/**
	 * 检查是否更新了，如果更新了。返回真，并记录这一次的文件修改时间
	 * @param path
	 * @return
	 */
	public ClassLoader classIsUpdate(String clsName){
		String path = clsToPath(clsName);
		
		String groovy_path = Config.getProperty("groovy", "groovy");
		File c = new File(groovy_path,path.replace(".class",".groovy")); //找groovy源代码
		if(c.exists()){
			path = c.getAbsolutePath();
		}else if(! FileTool.exists(path) && FileTool.exists("bin/") && ! path.contains("bin/") ){
			c = new File("bin",path);
		}

		long now = c.lastModified();
		if(last_tm.containsKey(path)){
			long last = last_tm.get(path);
			if(now > last){ //如果文件比较新，则创建新的loader
				last_tm.put(path,now);		
				return newLoader(c);
			}else{//否则用旧的
				if(path.endsWith(".groovy")){
					return groovyLoader;
				}else return loader;
			}
		}else{ //如果是第一次
			last_tm.put(path, now);
			
		}
		if(path.endsWith(".groovy")){
			return  groovyLoader != null? groovyLoader: newLoader(c);
		}else return loader != null? loader: newLoader(c);

		
	}
	private ClassLoader newLoader(File f){
		Log.d("load new file: "+f.getAbsolutePath());
		
		if(f.getName().endsWith(".groovy") && f.exists()){
			groovyLoader =   new GroovyClassLoader();
			return groovyLoader;
		}else{
			loader = new YangClassLoader(++loaderVersion,root,reloadPkg);
			return loader;
			
		}
	}

}
