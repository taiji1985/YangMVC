package org.docshare.boot;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.SessionIdManager;

public class YangSessionIdManager implements SessionIdManager {

	HashSet<String> set  =new HashSet<String>();
	
	@Override
	public void addLifeCycleListener(Listener arg0) {
		
	}

	@Override
	public boolean isFailed() {
		return false;
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public boolean isStarted() {
		return false;
	}

	@Override
	public boolean isStarting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStopping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeLifeCycleListener(Listener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean idInUse(String id) {
		return set.contains(id);
	}

	@Override
	public void addSession(HttpSession session) {
		
		
	}

	@Override
	public void removeSession(HttpSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void invalidateAll(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String newSessionId(HttpServletRequest request, long created) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClusterId(String nodeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNodeId(String clusterId, HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

}
