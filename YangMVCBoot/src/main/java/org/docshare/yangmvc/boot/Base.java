package org.docshare.yangmvc.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Base {
	private static final Logger logger = LoggerFactory.getLogger(Base.class);

	public void T(String msg){
		logger.info("{}", msg);
	}
}
