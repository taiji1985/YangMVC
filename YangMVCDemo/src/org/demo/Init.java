package org.demo;

import org.docshare.log.Log;

public class Init {
	/**
	 * 在此可以进行初始化。比如如果不愿意在web.xml中写死DB数据，可以在此直接修改Config.dbname等属性
	 */
	public Init(){ 
		Log.e("I can init something in Init class");
	}
}
