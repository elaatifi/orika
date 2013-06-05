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
package ma.glasnost.orika.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import ma.glasnost.orika.jsr166x.ConcurrentSkipListMap;


/**
 * A simple sorted collection implementation that allows for duplicates;
 * new items are inserted based on their comparison to existing items;
 * if a new item is found to be less than any item in the list, it is inserted 
 * before that item, else it is inserted at the end. <br><br>
 * The collection is backed by a ConcurrentSkipListMap, with the keys defined as
 * Double, allowing for inserts of items at a specific location with respect to
 * the other items in the map.
 * <p>
 * This class is thread-safe, with the same caveats for ConcurrentSkipListMap.
 * 
 * @author matt.deboer@gmail.com
 * @param <V> the element type contained in this list
 */
public class SortedCollection<V> implements Collection<V> {
    
    /**
     * 
     */
    protected final Ordering<V> ordering;
    /**
     * 
     */
    protected final ConcurrentSkipListMap<Double, V> sortedItems;
    
    /**
     * @param ordering
     */
    public SortedCollection(Ordering<V> ordering) {
        this.ordering = ordering;
        this.sortedItems = new ConcurrentSkipListMap<Double, V>();
    }
    
    /**
     * @param c the collection from which to initialize this SortedCollection
     * @param ordering the ordering used for sorting the elements
     */
    public SortedCollection(Collection<? extends V> c, Ordering<V> ordering) {
        this(ordering);
        addAll(c);
    }
    
    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    public synchronized boolean add(V value) {
        double index = 0;
        double nextIndex = 0;
        boolean insert = false;
        V current = null;
        
        for (Entry<Double, V> item: sortedItems.entrySet()) {
            current = item.getValue();
            int comparison = ordering.order(current, value);
            if (comparison > 0) {
                insert = true;
                nextIndex = item.getKey();
                break;
            } else {
                index = item.getKey();
            }
        }
        
        if (!insert) {
            nextIndex = index + 2.0;
        } 
        return insertBetween(index, nextIndex, value, true);
    }

    /**
     * Inserts the provided value with a key between the provided min and max values.
     * If an item is already found at that key, an additional comparison is performed,
     * and the new value is inserted at half the distance before or after the existing
     * key.
     * <p>
     * Returns true if the item is inserted.
     * 
     * @param min
     * @param max
     * @param value
     * @param allowDuplicates
     * @return true if the item was inserted
     */
    protected boolean insertBetween(Double min, Double max, V value, boolean allowDuplicates) {
        double key = min + ((max - min)/2.0);
        V existing = sortedItems.putIfAbsent(key, value);
        while (existing != null) {
            int comparison = ordering.order(existing, value);
            if (comparison > 0) {
                key = min + ((key - min) / 2.0);
            } else if (comparison < 0 || allowDuplicates || !existing.equals(value)) {
                key = key + ((max - key) / 2.0);
            } else {
                return false;
            }
            existing = sortedItems.putIfAbsent(key, value);
        }
        return true;
    }
    
    public int size() {
        return sortedItems.size();
    }
    
    public boolean isEmpty() {
        return sortedItems.isEmpty();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<V> iterator() {
        return sortedItems.values().iterator();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    public boolean contains(Object o) {
        return sortedItems.containsValue(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray()
     */
    public Object[] toArray() {
        return sortedItems.values().toArray();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#toArray(T[])
     */
    public <T> T[] toArray(T[] a) {
        return sortedItems.values().toArray(a);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    public boolean remove(Object o) {
        return sortedItems.values().remove(o);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    public boolean containsAll(Collection<?> c) {
        return sortedItems.values().containsAll(c);
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
        return sortedItems.values().removeAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return sortedItems.values().retainAll(c);
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    public void clear() {
        sortedItems.clear();
    }
    
    /**
     * @return the first item in this collection
     */
    public V first() {
        Entry<Double, V> entry = sortedItems.firstEntry();
        return entry != null ? entry.getValue() : null;
    }
    
    /**
     * @return the last item in this collection
     */
    public V last() {
        Entry<Double, V> entry = sortedItems.lastEntry();
        return entry != null ? entry.getValue() : null;
    }
}
