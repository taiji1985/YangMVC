package org.demo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.docshare.log.Log;
import org.docshare.mvc.Controller;
import org.docshare.util.FileTool;
import org.docshare.util.TextTool;

public class BigFileController extends Controller {
	public void index(){
		render();
	}
	public static class Chunk{
		@Override
		public String toString() {
			return "Chunk [name=" + name + ", index=" + index + ", blockFile="
					+ blockFile + "]";
		}
		public String name; //文件名称
		public int index;//索引
		public String blockFile;
		
	}
	private String randomFile(String oldFileName){
		String after = TextTool.getAfter(oldFileName, ".");
		
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/HHmmss");
		String path  =  "/upload/"+ df.format(new Date())+(int)(Math.random()*1000) +"."+after;
		return path;
	}
	private static Object lock = new Object();
	
	public void upload() throws IOException{
		
		Chunk c = new Chunk();
		c.name = param("name"); //文件登录
		c.index = paramInt("index"); //索引
		c.blockFile = param("file"); //字节内容存储位置
		
		if(c.blockFile == null){ //这时候要做合并
			String newFile = randomFile(c.name);
			FileOutputStream fos = new FileOutputStream(application.getRealPath(newFile));
			@SuppressWarnings("unchecked")
			HashMap<Integer, Chunk> cMap = (HashMap<Integer, Chunk>) sess("mfile_upload");
			for(int i=0;i<c.index;i++){
				Chunk chunk = cMap.get(i);
				if(chunk==null){
					Log.e("BB"+"chuck is null "+ i ," ha");
					break;
				}
				if(chunk.blockFile == null){
					Log.e("chuck block file is null , index = "+chunk.index,"");
					break; 
				}
				String realPath = application.getRealPath(chunk.blockFile);
				byte[] readed = FileTool.readBytes(realPath);
				fos.write(readed);
				FileTool.delFile(realPath);
			}
			output("succ");
			fos.close();
			sess("mfile_upload",null);
			return;
		}
		synchronized (lock) {
			@SuppressWarnings("unchecked")
			HashMap<Integer, Chunk> cMap = (HashMap<Integer, Chunk>) sess("mfile_upload");
			if(cMap == null)cMap =new HashMap<Integer, BigFileController.Chunk>();
			//if(c.index == 0) cMap.clear();
			cMap.put(c.index, c);
			Log.i("BigFile","upload " + c);
			sess("mfile_upload",cMap);
		}
		
		output("ok"+"upload " + c);
	}
}
