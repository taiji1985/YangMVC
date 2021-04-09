package org.docshare.mvc;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.docshare.log.Log;
import org.docshare.util.TextTool;

class UploadProcesser {

	private HttpServletRequest request;
	private Controller c;
	private ServletContext app = null;

	public UploadProcesser(Controller c ,HttpServletRequest request,HttpServletResponse response,ServletContext app)  {
		this.request =request;
		this.c = c;
		this.app  =app;
	}
	public void process() throws FileUploadException{
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		
		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = app;
		File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List<FileItem> items = upload.parseRequest(request);
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
		    FileItem item = iter.next();

		    if (item.isFormField()) {
		        processFormField(item);
		    } else {
		        processUploadedFile(item);
		    }
		}
		
	}
	private static  HashSet<String> forbitMap =new HashSet<String>();
	private String randomFileName(String oldFileName){
		//(Math.random()*100000)
		String after = TextTool.getLastAfter(oldFileName, ".");
		
		//阻止可执行在文件上传
		final String[] forbit = {"jsp","php","aspx","exe","sh"};
		if(forbitMap.isEmpty()){
			for(String s : forbit)forbitMap.add(s);
		}
		if(forbitMap.contains(after)) after += ".txt";
		
					
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/HHmmss");
		String path  =  "/upload/"+ df.format(new Date())+(int)(Math.random()*1000) +"."+after;
		
		return path;
	}
	@SuppressWarnings("unused")
	private void processUploadedFile(FileItem item) {
		String fieldName = item.getFieldName();
	    String fileName = item.getName();
	    String contentType = item.getContentType();
	    boolean isInMemory = item.isInMemory();
	    long sizeInBytes = item.getSize();
	    //String tmpdir = System.getProperty("java.io.tmpdir");
	    String sep=System.getProperty("line.separator");
	    //tmpdir = tmpdir.endsWith(sep)?tmpdir:tmpdir+sep;
	    String path = randomFileName(fileName);
	    String real = app.getRealPath(path);
	    
	    File f = new File(real);
		if(!f.getParentFile().exists()){
			Log.d("mkdir "+f.getParent());
			f.getParentFile().mkdirs();
		}
		
	    
	    File uploadedFile = new File(real);
	    try {
			item.write(uploadedFile);
			Log.d("write param "+ fieldName +" file to "+ real);
		} catch (Exception e) {
			Log.e(e);
		}
	    c.putParam(fieldName, path);
	    
	}
	private void processFormField(FileItem item) {

		String name = item.getFieldName();
	    String value = item.getString();
	    
	    c.putParam(name, transUtf8(value));
	}
	private String transUtf8(String p){
		try {
			return new String(p.getBytes("ISO-8859-1"),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return p;
	}
}
