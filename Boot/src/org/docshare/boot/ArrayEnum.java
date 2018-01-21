package org.docshare.boot;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;


public class ArrayEnum<E>  implements Enumeration<E>{

	private ArrayList<E> arr = null;
	int it = 0;
	public ArrayEnum(Set<E> set) {
		arr  = new ArrayList<E>(set);
	}
	
	public ArrayEnum(ArrayList<E> arr)  {
		this.arr = arr;
	}
	
	
	
	@Override
	public boolean hasMoreElements() {
		return it<arr.size();
	}

	@Override
	public E nextElement() {
		return arr.get(it++);
	}

}
