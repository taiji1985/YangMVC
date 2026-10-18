package org.docshare.yangmvc.log;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;


public class Log {
	private static final Logger logger = LoggerFactory.getLogger(Log.class);

    public static boolean showClass = false;
    public static String getCaller() {  
        StackTraceElement[] stack = (new Throwable()).getStackTrace(); 
        for(int i=0;i<stack.length-1;i++){
            String c = stack[i].getClassName();
            if(!c.equals("org.docshare.log.Log")){
                return c;
            }
        }
        return "Unknown";
    }  
    
    static java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Logger log = LoggerFactory.getLogger("YangMVC");
    public static String now(){
        return df.format(java.time.LocalDateTime.now());
    }
    public static String W(String t){
        if(!showClass) return t;
        String caller = getCaller();
        return "{"+caller+"} "+t;
    }
    public static  void i(Object str) {

        String t ;
        if(!(str instanceof String)){
            t = JSON.toJSONString(str,true);
        }else{
            t= str.toString();
        }
        log.info(W(t));
        
        //System.out.println(now()+ "[info ]" + str);
    }
    public static  void i(String ...arr){

        StringBuilder sBuffer =new StringBuilder();
        for(String t:arr){
            sBuffer.append(t);
        }
        log.info(W(sBuffer.toString()));
    }

    public static <T> void e(T i) {

        String s = i.toString();
        if(i instanceof Throwable){
            s = getErrMsg((Throwable) i);
        }
        
        log.error(W(s));
        
    }
    public static void e(String f , String...args){

        StringBuilder sBuffer =new StringBuilder();
        sBuffer.append(f);
        for(String t:args){
            sBuffer.append(t);
        }
        e(sBuffer.toString());
    }
    public static void e(String msg,Throwable err){
        String errorMsg = msg;
        if(err != null){
            errorMsg = msg + "\n" + getErrMsg(err);
        }
        log.error(W(errorMsg));
    }


    public static <T> void d(T str) { 

        log.debug(W(str.toString()));
    }
    public static  void d(String ...arr){

        StringBuilder sBuffer =new StringBuilder();
        for(String t:arr){
            sBuffer.append(t);
        }
        log.debug(W(sBuffer.toString()));
    }
    public static String getErrMsg(Throwable e){
        ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        PrintWriter pw = new java.io.PrintWriter(buf, true); 
        e.printStackTrace(pw);
        pw.flush();
        pw.close();

        return buf.toString();
    }
    
    @SuppressWarnings("rawtypes")
    public static void map(Map m){
        StringBuilder sb = new StringBuilder();
        for(Object k:m.keySet()){
            sb.append(',').append(k).append('=').append(m.get(k));
        }
        sb.append(']');
        sb.setCharAt(0, '[');
        sb.insert(0, "MAP");
        String s= sb.toString();
        Log.i(s);
    }

    public static void w(String m) {

        log.warn(W(m));
    }

    public static void i(String s, Object...args) {

        String str = String.format(s, args);
        log.info(str);
    }

    public static void v(Object obj) {

        log.trace(W(obj.toString()));
    }

}