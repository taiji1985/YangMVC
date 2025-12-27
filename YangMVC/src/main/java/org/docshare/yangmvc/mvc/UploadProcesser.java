package org.docshare.yangmvc.mvc;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.docshare.yangmvc.util.TextTool;

class UploadProcesser {
	private static final Logger logger = LoggerFactory.getLogger(UploadProcesser.class);


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
	    //tmpdir = tmpdir.endsWith(sep)?tmpdir:tmpdir+sep;
	    String path = randomFileName(fileName);
	    
	    // 获取 web 应用的根目录
	    String webRoot = app.getRealPath("/");
	    if (webRoot == null) {
	        // 如果无法获取 web 根目录，使用当前工作目录
	        webRoot = "./WebRoot/";
	    }
		String real = path;
		if(real.startsWith("/")){
			real = webRoot+real;
		}else{
			real = path;
		}
		real= real.replace("//", "/");
	    
	    // 构建完整的文件路径
	    //String real =  path.replace("/", File.separator);
	    
	    File uploadedFile = new File(real);
	    // 确保父目录存在
		if(!uploadedFile.getParentFile().exists()){
			logger.debug("mkdir "+uploadedFile.getParent());
			uploadedFile.getParentFile().mkdirs();
		}
	    try {
			item.write(uploadedFile);
			logger.debug("write param "+ fieldName +" file to "+ real);
		} catch (Exception e) {
			logger.error("", e);
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
