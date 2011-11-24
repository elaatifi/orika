package ma.glasnost.orika.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheLRULinkedHashMap<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {
    private static final long serialVersionUID = 6402613994118746566L;
    private final int maxSize;
    
    public CacheLRULinkedHashMap(int initialCapacity) {
        super(initialCapacity, .75F, true);
        
        maxSize = initialCapacity;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }
    
    public void cache(K key, V value) {
        put(key, value);
    }
}
