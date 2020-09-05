package org.docshare.mvc.anno;

import java.lang.annotation.*;  

@Target(ElementType.PARAMETER)  
@Retention(RetentionPolicy.RUNTIME)  
@Documented  
public @interface Param {  
    String value();
}  