package ma.glasnost.orika.converter.builtin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeFactory;

/**
 * CloneableConverter allows configuration of a number of specific types which
 * should be cloned directly instead of creating a mapped copy.<br><br>
 * 
 * This allows you to declare your own set of types which should be cloned instead
 * of mapped.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class CloneableConverter extends CustomConverter<Object, Object> {

	private final Set<Type<Cloneable>> clonedTypes = new HashSet<Type<Cloneable>>();
	private final Map<Class<?>, Method> cachedMethods;
	private final Method cloneMethod;
	/**
	 * Constructs a new ClonableConverter configured to handle the provided
	 * list of types by cloning.
	 * 
	 * @param types one or more types that should be treated as immutable
	 */
	public CloneableConverter(java.lang.reflect.Type...types) {
	    Method clone;
	    Map<Class<?>, Method> methodCache;
	    try {
            clone = Object.class.getDeclaredMethod("clone");
            clone.setAccessible(true);
            methodCache = null;
        } catch (SecurityException e) {
            clone = null;
            methodCache = new WeakHashMap<Class<?>, Method>();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
	    cloneMethod = clone;
	    cachedMethods = methodCache;
	    
	    for (java.lang.reflect.Type type: types) {
			clonedTypes.add(TypeFactory.<Cloneable>valueOf(type));
		}
	}
	
	private boolean shouldClone(Type<?> type) {
		for (Type<?> registeredType: clonedTypes) {
			if (registeredType.isAssignableFrom(type)) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ma.glasnost.orika.Converter#canConvert(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type)
	 */
	public boolean canConvert(Type<?> sourceType, Type<?> destinationType) {
	    return shouldClone(sourceType) && sourceType.equals(destinationType);
    }

	public Object convert(Object source, Type<? extends Object> destinationType) {
	    try {
	        Method clone;
	        if (cloneMethod != null) {
	            clone = cloneMethod;
	        } else {
	                clone = cachedMethods.get(source.getClass());
	                if (clone == null) {
	                    /*
	                     * Keep a cache of 'clone' methods based on the assumption that it's 
	                     * faster to lookup a method by source class than to call Class.getMethod
	                     * on that class.
	                     */
	                    synchronized(cachedMethods) {
	                        try {
	                            clone = source.getClass().getMethod("clone");
	                            cachedMethods.put(source.getClass(), clone);
	                        } catch (NoSuchMethodException e) {
	                            throw new IllegalStateException(e);
	                        } catch (SecurityException e) {
	                            throw new IllegalStateException(e);
	                        }
	                    }
	                }
	        }
	        return clone.invoke(source);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
