package org.docshare.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.docshare.log.Log;

public class IpHelper {
	public static void showIP() throws SocketException {
		Enumeration<?> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		while (allNetInterfaces.hasMoreElements()) {
			NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
					.nextElement();
			//System.out.println(netInterface.getName());
			Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
			while (addresses.hasMoreElements()) {
				ip = (InetAddress) addresses.nextElement();
				if (ip != null && ip instanceof Inet4Address) {
					Log.i("本机的IP = " + ip.getHostAddress());
				}
			}
		}
	}
	public static void main(String[] args) throws SocketException {
		showIP();
	}
	
	public static String showPortUsed(int port){
		Log.i("run command : netstat -aon");
		String command = String.format("netstat -aon",port);

		String sb = runCmd(command,false);
		String[] sa  =sb.split("\n");
		for(String s : sa){
			if(s.contains(port+"") && s.contains("LISTENING")){
				//find 
				Log.i(s);
				String[] saa = s.split(" ");
				Log.i("进程 "+saa[saa.length-1]+" 占用了 "+port+ " 端口 ");
				return saa[saa.length-1];
			}
		}
		return "";
		
			
	}
	private static String runCmd(String command,boolean waitFor) {
		Process p;
		StringBuffer sb = new StringBuffer();
		try {
			p = Runtime.getRuntime().exec(command);
			InputStream is = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String s = null;
			while ((s = reader.readLine()) != null) {
			  //  System.out.println(s);
			    sb.append(s+"\n");
			}	
			is.close();
			if(waitFor)p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	public static void killPID(String pid) {
		if(pid==null || pid.length() == 0) return;
		
		Log.i("正在尝试杀死占用端口的进程 , try to kill process that use the port ");
		String cmd = "taskkill /F /PID "+pid;
		Log.i(cmd);
		String r = runCmd(cmd,true);
		System.out.println(r);
	}
}
