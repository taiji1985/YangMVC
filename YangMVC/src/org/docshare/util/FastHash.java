package org.docshare.util;

import java.util.HashSet;

/**
 * 对字符串做key做了优化。
 * HashSet.contains操作时，先根据hash来定位，随后判断hash值是否相等，相等的再调用equals判断两个对象
 * 是否相等，如果能确信hashcode不会冲突，则不需要做这个长字符串的equals。
 * @author Administrator
 *
 */
public class FastHash {
	private HashSet<Integer> set;
	public void add(String key){
		set.add(key.hashCode());
	}
	public boolean contains(String key){
		return set.contains(key.hashCode());
	}

}
