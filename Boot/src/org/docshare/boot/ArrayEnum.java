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
	public ArrayEnum(){
		arr = new ArrayList<E>();
	}
	
	public ArrayEnum(ArrayList<E> arr)  {
		this.arr = arr;
	}
	
	@SuppressWarnings("unchecked")
	public void add( Enumeration<Object> e){
		
		while(e.hasMoreElements()){
			arr.add((E) e.nextElement());
		}
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
