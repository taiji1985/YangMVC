package org.docshare.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.docshare.log.Log;

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
    	Log.d("YangClassLoader "+version +",try load "+name);

        if(!name.startsWith(reloadPackage))
                return YangClassLoader.class.getClassLoader().loadClass(name);
        try {
//            String url = "file:C:/data/projects/tutorials/web/WEB-INF/" +
//                            "classes/reflection/MyObject.class";
//            URL myUrl = new URL(url);
//            URLConnection connection = myUrl.openConnection();
//            InputStream input = connection.getInputStream();
        	
        
        	//FileInputStream input = new FileInputStream(name2Path(name));
        	//Log.d(name2Path(name));
        	Log.d(getClass().getResource(name2Path(name)));
        	
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        	InputStream input = getClass().getResourceAsStream(name2Path(name));
        	if(input == null){
        		return null;
        	}
        	//int data = input.read();
        	byte[] buf =new byte[1024];
            int num = input.read(buf);
            
        	while(num > 0){
                buffer.write(buf,0,num);
                num = input.read(buf);
            }

            input.close();

            byte[] classData = buffer.toByteArray();

            return defineClass(name,
                    classData, 0, classData.length);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
