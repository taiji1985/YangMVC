package org.docshare.yangmvc.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.docshare.yangmvc.log.Log;

class YangClassLoader extends ClassLoader{
    private int version;
	private Object root;
	private String reloadPackage;
	public YangClassLoader(int v,String clsRoot,String reloadPackage) {
    	this.version = v;
    	if(! clsRoot.endsWith("/")){
    		clsRoot+= "/";
    	}
    	this.root = clsRoot;
    	this.reloadPackage = reloadPackage;
	}
	public String name2Path(String cname){
		return root + cname.replace(".", "/")+".class";
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
    	
			// First check if the class is already loaded
			Class<?> loadedClass = findLoadedClass(name);
			if (loadedClass != null) {
				return loadedClass;
			}
			
			if(!name.startsWith(reloadPackage) ){ 
				Class<?> ret= YangClassLoader.class.getClassLoader().loadClass(name);			
				//MethodAccessCacher.put(name, ret);
				return ret;
			}

			// else if(false){ //for debug
 			// 	Class<?> ret= YangClassLoader.class.getClassLoader().loadClass(name);			
			// 	MethodAccessCacher.put(name, ret);
			// 	return ret;
			// }

			try {
				Log.d("YangClassLoader v"+version +", load "+name);
	//            String url = "file:C:/data/projects/tutorials/web/WEB-INF/" +
	//                            "classes/reflection/MyObject.class";
	//            URL myUrl = new URL(url);
	//            URLConnection connection = myUrl.openConnection();
	//            InputStream input = connection.getInputStream();
				Class<?> ret;
				String path = name2Path(name);
				URL url = getClass().getResource(path);
				
				
					//FileInputStream input = new FileInputStream(name2Path(name));
					
				Log.d("YangClassLoader "+url);
				
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				InputStream input = getClass().getResourceAsStream(path);
				if(input == null){
					return null;
				}
				//int data = input.read();
				byte[] buf =new byte[10240];
				int num = input.read(buf);
				
				while(num > 0){
					buffer.write(buf,0,num);
					num = input.read(buf);
				}

				input.close();

				byte[] classData = buffer.toByteArray();

				Log.d("YangClassLoader defined class");
				ret = defineClass(name,	classData, 0, classData.length);
				
				
				Log.d("YangClassLoader loaded ,try put MethodAccessCacher "+url);
				MethodAccessCacher.asyncPut(name, ret);
				Log.d("YangClassLoader MethodAccessCacher loaded ,try put MethodAccessCacher"+url);
				return ret;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        

        return null;
    }
}
