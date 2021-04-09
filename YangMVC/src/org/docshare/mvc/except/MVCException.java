package org.docshare.mvc.except;

import java.io.PrintStream;
import java.io.PrintWriter;

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
			throwable.printStackTrace(s);
			s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~");
		}
		super.printStackTrace(s);
	}
	@Override
	public void printStackTrace(PrintWriter s) {
		s.println("Message: "+msg);
		if(throwable!=null){
			throwable.printStackTrace(s);
			s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~");
		}
		super.printStackTrace(s);
	}

}
