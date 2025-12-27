package cn.org.docshare.demo;

import org.docshare.yangmvc.boot.YangMVCWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Init {
	private static final Logger logger = LoggerFactory.getLogger(Init.class);

	public Init() {
		logger.info("init called");
		YangMVCWebSocketHandler.register(MyWebSocketListener.class);
	}
}
