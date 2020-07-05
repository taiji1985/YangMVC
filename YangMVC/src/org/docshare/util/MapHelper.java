package org.docshare.util;

import java.util.HashMap;

public class MapHelper {
	/**
	 * 可变参数转hashmap
	 * @param v
	 * @return
	 */
	public static HashMap<String, Object> toMap(Object ...v){
		HashMap<String, Object>  ret = new HashMap<String, Object>();
		for(int i=0;i<v.length;i+=2){
			if(i<v.length-1){
				ret.put(v[i].toString(),v[i+1]);
			}
		}
		return ret;
	}
}
