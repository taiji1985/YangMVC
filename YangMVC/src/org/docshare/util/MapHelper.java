package org.docshare.util;

import java.util.HashMap;

public class MapHelper {
	/**
	 * 可变参数转hashmap
	 * @param v 交替存放key和value。如 a,1,b,2 表述a=1,b=2
	 * @return 哈希表
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
