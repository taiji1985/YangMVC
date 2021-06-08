package org.docshare.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;



public class Poster {
	public static String doPostJSON(String url,Object params, String token) {
		String pp=JSON.toJSONString(params);
		try {
			return doPost(url, pp,token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static String doPost(String url, Map<String, Object> params) {
		try {
			return doPost(url, params,null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	static final boolean useProxy = false;
	public static String doPost(String url,String params,String token) throws Exception {
		// 编码请求参数
        
        URL restURL = new URL(url);
        
        Log.d("链接："+url);
        int t= 0;//调用接口出错次数
        while(t<1) {
            try {
                /*
                 * 此处的urlConnection对象实际上是根据URL的请求协议(此处是http)生成的URLConnection类
                 * 的子类HttpURLConnection
                 */
            	HttpURLConnection conn=null;
            	if(useProxy){
	            	InetSocketAddress addr = new InetSocketAddress("127.0.0.1",8080);  
	                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
	                conn = (HttpURLConnection) restURL.openConnection(proxy);
            	}else{
            		conn = (HttpURLConnection) restURL.openConnection();
            	}
//                conn.setConnectTimeout(5000);
//                conn.setReadTimeout(5000);
                // 请求方式
                conn.setRequestMethod("POST");
                // 设置是否从httpUrlConnection读入，默认情况下是true; httpUrlConnection.setDoInput(true);
                conn.setDoOutput(true);
                // allowUserInteraction 如果为 true，则在允许用户交互（例如弹出一个验证对话框）的上下文中对此 URL 进行检查。
                conn.setAllowUserInteraction(false);
                conn.addRequestProperty("Content-Type", "application/json;charset=utf-8");
                if(token!=null){
                	conn.addRequestProperty("token", token);
                	conn.addRequestProperty("Cookie", "JSESSIONID="+ token);
                }

                PrintStream ps = new PrintStream(conn.getOutputStream());
                ps.print(params);

                ps.close();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                String line, resultStr = "";

                while (null != (line = bReader.readLine())) {
                    resultStr += line;
                }
                System.out.println(resultStr);
                bReader.close();

                return resultStr;
            } catch (Exception e) {
                t++;
                Log.e("=======>出错次数(requset error )：" + t);
                e.printStackTrace();
            }
        }
        
        throw new Exception("网络异常,请稍后再试");

    }
    
    /**
     * 参数类型转换
     * 
     * @param parameters
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String paramsConvert(Map<String, Object> parameters) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();// 处理请求参数
        // 编码请求参数
        for (String name : parameters.keySet()) {
            Object object = parameters.get(name);
            Log.d("类型："+object.getClass());
            if(object instanceof Integer) {
                sb.append(name).append("=").append(java.net.URLEncoder.encode((String)object, "UTF-8"))
                .append("&");
            }else if(object instanceof Date) {
                Calendar cal = Calendar.getInstance();
                cal.setTime((Date)object);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sb.append(name).append("=").append(java.net.URLEncoder.encode(sdf.format(cal.getTime()), "UTF-8"))
                .append("&");
            }else{
                if(object.getClass().isArray()) {//判断是否是字符串数组
                    String[] values = (String[])object;
                    for(String va : values) {
                        sb.append(name).append("=").append(java.net.URLEncoder.encode((String) va, "UTF-8"))
                        .append("&");
                    }
                }else {
                    sb.append(name).append("=").append(java.net.URLEncoder.encode(""+parameters.get(name), "UTF-8"))
                    .append("&");
                }
            }
        }
        return sb.toString().substring(0, sb.toString().length()-1);
	}    
    
    public static String doPost(String url, Map<String, Object> parameters,String token) throws Exception {
        String params = paramsConvert(parameters);
        Log.d("参数："+params.toString());
        return doPost(url, params,token);
    }
    
    public static void main(String[] args) throws Exception {
    	Map<String,String> map = new HashMap<>();
    	map.put("loginName"	, "admin");
    	map.put("password", "123456");
    	//Log.i(Poster.doPostJSON("http://localhost:8001/shiro-api/login", map,null));
    	String token = "15D1821DFC4851E0E67C85D5335A68BA";
    	Log.i(Poster.doPostJSON("http://localhost:8001/shiro-api/check?token="+token,"",token));
    }
}