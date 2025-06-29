package org.docshare.yangmvc.mvc.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)  
public @interface RequestMapping {
	public String url() ; 
}
