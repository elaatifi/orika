/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ma.glasnost.orika.util.Ordering.OrderingRelation;

/**
 * A simple sorted collection implementation that allows for duplicates; new
 * items are inserted based on their comparison to existing items; if a new item
 * is found to be less than any item in the list, it is inserted before that
 * item, else it is inserted at the end. <br>
 * <br>
 * The collection is backed by a ConcurrentSkipListMap, with the keys defined as
 * Double, allowing for inserts of items at a specific location with respect to
 * the other items in the map.
 * <p>
 * This class is thread-safe, with the same caveats for ConcurrentSkipListMap.
 * 
 * @author matt.deboer@gmail.com
 * @param <V>
 *            the element type contained in this list
 */
public class SortedCollection<V> implements Collection<V> {

	/**
     * 
     */
	protected final Ordering<V> ordering;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final ReentrantReadWriteLock sortLock = new ReentrantReadWriteLock();

	private Set<Node<V>> nodes = new LinkedHashSet<Node<V>>();

	private List<V> items = new ArrayList<V>();

	private volatile List<V> sortedItems = null;

	/**
	 * @param ordering
	 */
	public SortedCollection(Ordering<V> ordering) {
		this.ordering = ordering;
	}

	/**
	 * @param c
	 *            the collection from which to initialize this SortedCollection
	 * @param ordering
	 *            the ordering used for sorting the elements
	 */
	public SortedCollection(Collection<? extends V> c, Ordering<V> ordering) {
		this(ordering);
		addAll(c);
	}

	public boolean add(V value) {
		try {
			rwl.writeLock().lock();
			if (mustAdd(value)) {
				items.add(value);
				Node<V> newNode = new Node<V>(value);
				for (Node<V> from : nodes) {
					final OrderingRelation order = ordering.order(
							from.getValue(), newNode.getValue());
					if (order == OrderingRelation.AFTER) {
						from.addEdge(newNode);
					} else if (order == OrderingRelation.BEFORE) {
						newNode.addEdge(from);
					}
				}
				nodes.add(newNode);
				sortedItems = null;
				return true;
			}
			return false;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	protected boolean mustAdd(V item) {
		return true;
	}

	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	public void clear() {
		try {
			rwl.writeLock().lock();
			nodes.clear();
			items.clear();
			sortedItems = null;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	private List<V> getSortedItems() {
		if (sortedItems == null) {
			try {
				sortLock.writeLock().lock();
				if (sortedItems == null)
					sortedItems = TopologicalSorter.sort(nodes);
			} finally {
				sortLock.writeLock().unlock();
			}
		}
		return sortedItems;
	}

	public int size() {
		try {
			rwl.readLock().lock();
			return items.size();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean isEmpty() {
		try {
			rwl.readLock().lock();
			return items.isEmpty();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public Iterator<V> iterator() {
		try {
			rwl.readLock().lock();
			return getSortedItems().iterator();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean contains(Object o) {
		try {
			rwl.readLock().lock();
			return items.contains(o);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public Object[] toArray() {
		try {
			rwl.readLock().lock();
			return getSortedItems().toArray();
		} finally {
			rwl.readLock().unlock();
		}
	}

	public <T> T[] toArray(T[] a) {
		try {
			rwl.readLock().lock();
			return getSortedItems().toArray(a);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean containsAll(Collection<?> c) {
		try {
			rwl.readLock().lock();
			return items.containsAll(c);
		} finally {
			rwl.readLock().unlock();
		}
	}

	public boolean addAll(Collection<? extends V> c) {
		try {
			rwl.writeLock().lock();
			boolean ret = false;
			for (V curItem : c) {
				ret |= add(curItem);
			}
			return ret;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	public boolean removeAll(Collection<?> c) {
		try {
			rwl.writeLock().lock();
			boolean ret = false;
			for (Object curItem : c) {
				ret |= remove(curItem);
			}
			return ret;
		} finally {
			rwl.writeLock().unlock();
		}
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the first item in this collection
	 */
	public V first() {
		try {
			rwl.readLock().lock();
			return items.isEmpty() ? null : getSortedItems().get(0);
		} finally {
			rwl.readLock().unlock();
		}
	}
}
