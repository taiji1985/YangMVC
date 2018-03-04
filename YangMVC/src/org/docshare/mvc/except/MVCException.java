package org.docshare.mvc.except;

import java.io.PrintStream;
import java.io.PrintWriter;

public class MVCException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4515480661510346678L;
	private Throwable throwable = null;

	public MVCException(String msg) {
		super(msg);
	}
	public MVCException(Throwable throwable){
		this.throwable  = throwable;
	}
	
	@Override
	public void printStackTrace(PrintStream s) {
		if(throwable!=null){
			throwable.printStackTrace(s);
			s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~");
		}
		super.printStackTrace(s);
	}
	@Override
	public void printStackTrace(PrintWriter s) {
		if(throwable!=null){
			throwable.printStackTrace(s);
			s.println("~~~~~~~~~~~~  The Message is above ~~~~~~~~~~~");
		}
		super.printStackTrace(s);
	}

}
