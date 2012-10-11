package ma.glasnost.orika.converter.builtin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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
	private final Method cloneMethod;
	/**
	 * Constructs a new ClonableConverter configured to handle the provided
	 * list of types by cloning.
	 * 
	 * @param types one or more types that should be treated as immutable
	 */
	public CloneableConverter(java.lang.reflect.Type...types) {
		try {
            cloneMethod = Object.class.getDeclaredMethod("clone");
            cloneMethod.setAccessible(true);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
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
            return cloneMethod.invoke(source);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
