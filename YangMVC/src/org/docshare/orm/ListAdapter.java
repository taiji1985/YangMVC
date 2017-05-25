package org.docshare.orm;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.docshare.log.Log;


class ListAdapter implements List<Model> {

	@Override
	public int size() {
		Log.d("size() called");
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		Log.d("isEmpty() called");
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		Log.d("contains() called");
		return false;
	}

	@Override
	public Iterator<Model> iterator() {
		// TODO Auto-generated method stub
		Log.d("iterator() called");
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(Model e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends Model> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Model> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public Model set(int index, Model element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(int index, Model element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Model remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<Model> listIterator() {
		// TODO Auto-generated method stub
		Log.d("ListIterator() called");
		return null;
	}

	@Override
	public ListIterator<Model> listIterator(int index) {
		// TODO Auto-generated method stub
		Log.d("ListIterator() called");
		return null;
	}

	@Override
	public List<Model> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Model get(int index) {
		// TODO Auto-generated method stub
		Log.d("get() called");
		return null;
	}

}
