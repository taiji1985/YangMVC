package org.docshare.yangmvc.boot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpHelper {
	private static final Logger logger = LoggerFactory.getLogger(IpHelper.class);

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
					logger.info("Local IP = " + ip.getHostAddress());
				}
			}
		}
	}
	public static void main(String[] args) throws SocketException {
		showIP();
	}
	
	public static String showPortUsed(int port){
		logger.info("run command : netstat -aon");
		String command = "netstat -aon";//String.format("netstat -aon",port);

		String sb = runCmd(command,false);
		String[] sa  =sb.split("\n");
		for(String s : sa){
			if(s.contains(port+"") && s.contains("LISTENING")){
				//find 
				logger.info("{}", s);
				String[] saa = s.split(" ");
				logger.info("Process "+saa[saa.length-1]+" is using port "+port);
				return saa[saa.length-1];
			}
		}
		return "";
		
			
	}
	private static String runCmd(String command,boolean waitFor) {
		Process p;
		StringBuilder sb = new StringBuilder();
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
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
		return sb.toString();
	}
	public static void killPID(String pid) {
		if(pid==null || pid.length() == 0) return;
		
		logger.info("Trying to kill process that is using the port");
		String cmd = "taskkill /F /PID "+pid;
		logger.info("{}", cmd);
		String r = runCmd(cmd,true);
		logger.info("{}", r);
	}
}
