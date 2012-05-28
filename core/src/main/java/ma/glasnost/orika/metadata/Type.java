package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Collection;

import ma.glasnost.orika.impl.util.ClassUtil;

/**
 * Type is an implementation of ParameterizedType which may be
 * used in various mapping methods where a Class instance would normally
 * be used, in order to provide more specific details as to the actual types
 * represented by the generic template parameters in a given class.<br><br>
 * 
 * Such details are not normally available at runtime using a Class instance
 * due to type-erasure.<br><br>
 * 
 * Type essentially provides a runtime token to represent a ParameterizedType
 * with fully-resolve actual type arguments; it will contain 
 * 
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public final class Type<T> implements ParameterizedType {
    
    private final Class<T> rawType;
    //private java.lang.reflect.Type ownerType;
    private final Type<?>[] actualTypeArguments;
    private final boolean isParameterized;
    private Map<String, Type<?>> typesByVariable;
    private Type<?> superType;
    private Type<?>[] interfaces;
    private Type<?> componentType;
    private final TypeKey key;

    /**
     * @param rawType
     * @param actualTypeArguments
     */
    @SuppressWarnings("unchecked")
    Type(TypeKey key, Class<?> rawType, Map<String, Type<?>> typesByVariable, Type<?>... actualTypeArguments) {
        this.key = key;
        this.rawType = (Class<T>)rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.typesByVariable = typesByVariable;
        this.isParameterized = rawType.getTypeParameters().length > 0;
    }
    
    /**
     * @return true if the given type is parameterized by nested types
     */
    public boolean isParameterized() {
        return isParameterized;
    }
    
    private Type<?> resolveGenericAncestor(java.lang.reflect.Type ancestor) {
    	Type<?> resolvedType = null;
		if (ancestor instanceof ParameterizedType) {
			resolvedType = TypeFactory.resolveValueOf((ParameterizedType)ancestor, this);
		} else if (ancestor instanceof Class) {
			resolvedType = TypeFactory.valueOf((Class<?>)ancestor);
		} else if (ancestor == null){
		    resolvedType = TypeFactory.TYPE_OF_OBJECT;
		} else {
			throw new IllegalStateException("super-type of " + this.toString() + 
					" is neither Class, nor ParameterizedType, but " + ancestor);
		}
		return resolvedType;
    }
    
    /**
     * Get the nested Type of the specified index.
     * 
     * @param index
     * @return
     */
    @SuppressWarnings("unchecked")
	public <X> Type<X> getNestedType(int index) {
    	return (Type<X>)((index > -1 && actualTypeArguments.length > index) ? actualTypeArguments[index] : null);
    }
    
    /**
     * @return the direct super-type of this type, with type arguments resolved with 
     * respect to the actual type arguments of this type.
     * 
     */
    public Type<?> getSuperType() {
    	if (this.superType == null) {
    		synchronized(this) {
	    		if (this.superType == null) {
	    			this.superType = resolveGenericAncestor(rawType.getGenericSuperclass());
	    		}
    		}
    	}
        return this.superType;
    }
    
    /**
     * @return the interfaces implemented by this type, with type arguments resolved with
     * respect to the actual type arguments of this type.
     */
    public Type<?>[] getInterfaces() {
    	if (this.interfaces == null) {
    		synchronized(this) {
	    		if (this.interfaces == null) {
	    			this.interfaces = new Type<?>[rawType.getGenericInterfaces().length];
		    		int i=0;
		    		for (java.lang.reflect.Type interfaceType: rawType.getGenericInterfaces()) {
		    			this.interfaces[i++] = resolveGenericAncestor(interfaceType);
		    		}
	    		}
    		}
    	}
    	return interfaces;
    }

    /* (non-Javadoc)
     * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
     */
    public java.lang.reflect.Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }
    
    public Map<String, Type<?>> getTypesByVariable() {
    	return Collections.unmodifiableMap(typesByVariable);
    }
    
    public java.lang.reflect.Type getTypeByVariable(TypeVariable<?> typeVariable) {
        if (isParameterized) {
            return typesByVariable.get(typeVariable.getName());
        } else {
            return null;
        }
    }
    
    public Class<T> getRawType() {
        return rawType;
    }
    
    public Type<?> getComponentType() {
    	if (componentType == null) {
            if (rawType.isArray()) {
            	componentType = TypeFactory.valueOf(rawType.getComponentType());
            } else if (isParameterized){
            	componentType = this.getNestedType(0);
            }
        }
        return componentType;
    }
    
    public java.lang.reflect.Type getOwnerType() {
    	throw new UnsupportedOperationException();
    }
    
    public String getSimpleName() {
        return this.rawType.getSimpleName();
    }
    
    public String getName() {
        return this.rawType.getName();
    }
    
    public String getCanonicalName() {
        return this.rawType.getCanonicalName();
    }
    
    /**
     * Test whether this type is assignable from the other type.
     * 
     * @param other
     * @return
     */
    public boolean isAssignableFrom(Type<?> other) {
        if (other==null) {
            return false;
        }
        if (!this.getRawType().isAssignableFrom(other.getRawType())) {
            return false;
        }
        if (!this.isParameterized && other.isParameterized) {
            return true;
        } else if (this.rawType.equals(Enum.class) && other.isEnum()){
            return true;
        } else {
        
            if (this.getActualTypeArguments().length!=other.getActualTypeArguments().length) {
                return false;
            }
            java.lang.reflect.Type[] thisTypes = this.getActualTypeArguments();
            java.lang.reflect.Type[] thatTypes = other.getActualTypeArguments();
            for (int i=0, total=thisTypes.length; i < total; ++i ) {
                Type<?> thisType = (Type<?>)thisTypes[i];
                Type<?> thatType = (Type<?>)thatTypes[i];
                // Note: this may be less strict than the rules for compile-time
                // assignability of generic types, but we're only interested in
                // actual runtime types
            	if (!thisType.isAssignableFrom(thatType)) {
            		return false;
            	}   
            }
            return true;
        }
    }
    
    /**
     * Test whether this type is assignable from the other Class;
     * returns true if this type is not parameterized and
     * the raw type is assignable.
     * 
     * @param other
     * @return
     */
    public boolean isAssignableFrom(Class<?> other) {
    	if (other==null) {
            return false;
        }
        if (this.isParameterized()) {
            return false;
        }
        return this.getRawType().isAssignableFrom(other);
    }
    
    public boolean isEnum() {
    	return getRawType().isEnum();
    }
   
    public boolean isArray() {
    	return getRawType().isArray();
    }
    
    public boolean isCollection() {
    	return Collection.class.isAssignableFrom(getRawType());
    }
    
    public boolean isMap() {
    	return Map.class.isAssignableFrom(getRawType());
    }
    
    public boolean isPrimitive() {
    	return getRawType().isPrimitive();
    }
    
    public boolean isPrimitiveWrapper() {
    	return ClassUtil.isPrimitiveWrapper(getRawType());
    }
    
    public boolean isConvertibleFromString() {
    	return ClassUtil.isConvertibleFromString(getRawType());
    }
    
    public String toString() {
    	StringBuilder stringValue = new StringBuilder();
    	stringValue.append(rawType.getSimpleName());
    	if (actualTypeArguments.length > 0) {
    		stringValue.append("<");
    		for (java.lang.reflect.Type arg: actualTypeArguments) {
    			stringValue.append(""+arg + ", ");
    		}
    		stringValue.setLength(stringValue.length()-2);
    		stringValue.append(">");
    	}
    	
    	return stringValue.toString();
    }
    
    public String toFullyQualifiedString() {
        StringBuilder stringValue = new StringBuilder();
        stringValue.append(rawType.getCanonicalName());
        if (actualTypeArguments.length > 0) {
            stringValue.append("<");
            for (java.lang.reflect.Type arg: actualTypeArguments) {
                stringValue.append(""+arg + ", ");
            }
            stringValue.setLength(stringValue.length()-2);
            stringValue.append(">");
        }
        
        return stringValue.toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(actualTypeArguments);
        result = prime * result + ((rawType == null) ? 0 : rawType.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Type<?> other = (Type<?>) obj;
        
        return this.key.equals(other.key);
//        if (!this.key.equals(other.key)) {
//            // shortcut
//            return false;
//        }
//        
//        if (rawType == null) {
//            if (other.rawType != null) {
//                return false;
//            }
//        } else if (!rawType.equals(other.rawType)) {
//            return false;
//        }
//        
//        if (!Arrays.equals(actualTypeArguments, other.actualTypeArguments)) {
//            return false;
//        }
//        
//        return true;
    }
}
