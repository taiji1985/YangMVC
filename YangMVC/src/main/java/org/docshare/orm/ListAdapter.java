package org.docshare.orm;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;


abstract class ListAdapter implements List<Model> {

	public List<Model> toArrayList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addAll(int index, Collection<? extends Model> c) {
		List<Model> list = toArrayList();
		list.addAll(index,c);
		return true;
	}
	@Override
	public boolean addAll(Collection<? extends Model> c) {
		List<Model> list = toArrayList();
		list.addAll(c);
		return true;
	}
	@Override
	public Object[] toArray() {
		
		return toArrayList().toArray();
	}
	@Override
	public <T> T[] toArray(T[] a) {
		return toArrayList().toArray(a);
	}
	@Override
	public boolean add(Model e) {
		return toArrayList().add(e);
	}
	@Override
	public boolean remove(Object o) {
		return toArrayList().remove(o);
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		return toArrayList().containsAll(c);
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		return toArrayList().removeAll(c);
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return toArrayList().retainAll(c);
	}
	@Override
	public void clear() {
		toArrayList().clear();
	}
	@Override
	public Model set(int index, Model element) {
		return toArrayList().set(index, element);
	}
	@Override
	public void add(int index, Model element) {
		toArrayList().add(index, element);
		
	}
	@Override
	public Model remove(int index) {
		return toArrayList().remove(index);
	}
	@Override
	public int indexOf(Object o) {
		return toArrayList().indexOf(o);
	}
	@Override
	public int lastIndexOf(Object o) {
		return toArrayList().lastIndexOf(o);
	}
	@Override
	public ListIterator<Model> listIterator() {
		return toArrayList().listIterator();
	}
	@Override
	public ListIterator<Model> listIterator(int index) {
		return toArrayList().listIterator(index);
	}
	@Override
	public List<Model> subList(int fromIndex, int toIndex) {
		return toArrayList().subList(fromIndex, toIndex);
	}

}
