/**
 * Copyright 2008 YangTongfeng
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software")
 * , to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
	public static HashMap<String, Object> empty(){
        return new HashMap<String,Object>();
    }
}
