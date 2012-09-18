/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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
package ma.glasnost.orika.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A simple sorted collection implementation that allows for duplicates;
 * new items are inserted based on their comparison to existing items;
 * if a new item is found to be less than any item in the list, it is inserted 
 * before that item, else it is inserted at the end. <br><br>
 * The collection is backed by a LinkedList, and all operations save for <code>add</code>
 * and <code>addAll</code> perform in the same time as the backing collection.<br><br>
 * The add method performs in linear time (O(n)), and the addAll method performs in
 * O(n*m) time, where n is the current size of the list, and m is the count being added.
 * 
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class SortedCollection<V> implements Collection<V> {
    
    private Comparator<V> comparator = null;
    private LinkedList<V> sortedList = new LinkedList<V>();
    
    /**
     * 
     */
    public SortedCollection() {
   
    }
    
    /**
     * @param comparator
     */
    public SortedCollection(Comparator<V> comparator) {
        this.comparator = comparator;
    }
    
    public boolean add(V value) {
        int i = -1;
        for (V item: sortedList) {
            ++i;
            int comparison = comparator == null ? toComparable(item).compareTo(value) : comparator.compare(item, value);
            if (comparison > 0) {
                sortedList.add(i, value);
                return true;
            }
        }
        sortedList.addLast(value);
        return true;
    }

    public int size() {
        return sortedList.size();
    }
    
    public boolean isEmpty() {
        return sortedList.isEmpty();
    }
    
    @SuppressWarnings({"unchecked" })
    private static <V> Comparable<V> toComparable(V item) {
        return (Comparable<V>)item;
    }
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<V> iterator() {
        return sortedList.iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return sortedList.contains(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return sortedList.toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return sortedList.toArray(a);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return sortedList.remove(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return sortedList.containsAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends V> c) {
        for (V value: c) {
            add(value);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    public boolean removeAll(Collection<?> c) {
        return sortedList.removeAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return sortedList.retainAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        sortedList.clear();
    }
    
    /**
     * @return the first item in this collection
     */
    public V first() {
        return sortedList.getFirst();
    }
    
    /**
     * @return the last item in this collection
     */
    public V last() {
        return sortedList.getLast();
    }
}
