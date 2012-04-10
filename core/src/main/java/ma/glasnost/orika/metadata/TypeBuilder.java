package ma.glasnost.orika.metadata;

import java.lang.reflect.ParameterizedType;

/**
 * TypeBuilder is a class used to create a fully populated Type instance
 * based on it's generic declaration.<br><br>
 * 
 * For example, to create a <code>Type&lt;A,&lt;B,C&gt;&gt;</code>, one would use the following:
 * <pre>
 * new TypeBuilder&lt;A,&lt;B,C&gt;&gt;(){}.build();
 * </pre>
 * @author matt.deboer@gmail.com
 *
 * @param <T>
 */
public abstract class TypeBuilder<T> {
    
    private final Class<T> rawType;
    private final java.lang.reflect.Type[] actualTypeArguments;

    @SuppressWarnings("unchecked")
    public TypeBuilder() {
       
        ParameterizedType parameterizedType = (ParameterizedType)getClass().getGenericSuperclass();
        java.lang.reflect.Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof ParameterizedType) {
            parameterizedType = (ParameterizedType)type;
            this.rawType = (Class<T>)parameterizedType.getRawType();
            this.actualTypeArguments = parameterizedType.getActualTypeArguments();
        } else {
            this.rawType = (Class<T>)type;
            this.actualTypeArguments = new java.lang.reflect.Type[0];
        }
    }
    
    public final Type<T> build() {
    	return TypeFactory.valueOf(rawType, actualTypeArguments);
    }

}
