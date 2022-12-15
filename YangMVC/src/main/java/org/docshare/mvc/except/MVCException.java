package org.docshare.mvc.except;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.docshare.util.TextTool;


public class MVCException extends RuntimeException{
	private String msg = "";
	/**
	 * 
	 */
	private static final long serialVersionUID = -4515480661510346678L;
	private Throwable throwable = null;

	public MVCException(String msg) {
		super(msg);
		this.msg = msg;
	}
	public MVCException(Throwable throwable){
		this.throwable  = throwable;
	}
	public MVCException(String msg,Throwable throwable){
		super(msg);
		this.msg = msg;
		this.throwable = throwable;
	}
	public MVCException(String...args) {
		super();
		this.msg = TextTool.concat((Object[])args).toString();
	}
	
	@Override
	public void printStackTrace(PrintStream s) {
		s.println("Message: "+msg);
		if(throwable!=null){
			s.print(toString(throwable));
			s.flush();
			return;
			//s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~");
		}
		super.printStackTrace(s);
	}
	@Override
	public void printStackTrace(PrintWriter s) {
		s.println("Message: "+msg);
		if(throwable!=null){
			//throwable.printStackTrace(s);
			s.print(toString(throwable));
			s.flush();
			return;
			//s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~"); 
		}
		super.printStackTrace(s);
	}
	private String toString(Throwable t){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter pw  =new PrintWriter(bos);
		t.printStackTrace(pw);
		pw.flush();
		pw.close();
		
		String str = new String(bos.toByteArray());
		StringBuilder sb =new StringBuilder();
		for(String s:str.split("\n")){
			if(s.contains("org.eclipse.jetty")){
				break;
			}
			sb.append(s+"\n");
		}
		return sb.toString();
		
	}

}
