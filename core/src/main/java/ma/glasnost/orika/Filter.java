package ma.glasnost.orika;

import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * @author mattdeboer
 *
 * @param <A>
 * @param <B>
 */
public interface Filter<A, B> extends MappedTypePair<A, B>{
    
    /**
     * This method will be called to determine if this interceptor should be
     * applied to the mapping of the specified properties
     * 
     * @param source the source property
     * @param destination the destination property
     * @return true if this Interceptor applies
     */
    public boolean appliesTo(Property source, Property destination);
    
    /**
     * @return true if this Interceptor should be called to filter the source
     * value the mapping results
     */
    public boolean filtersSource();
    
    /**
     * @return true if this Interceptor should be called to filter the destination
     * of the mapping results
     */
    public boolean filtersDestination();
    
    /**
     * 
     * 
     * @param sourceType
     * @param sourceName
     * @param destType
     * @param destName
     * @param mappingContext the current mapping context
     * @return true if the fields represented by these types and names
     */
    public boolean shouldMap(Type<?> sourceType, String sourceName, Type<?> destType, String destName, MappingContext mappingContext);
    
    /**
     * Filters the output value
     * 
     * @param destinationValue
     * @param sourceType
     * @param sourceName
     * @param destType
     * @param destName
     * @param mappingContext the current mapping context
     * @return the filtered output value
     */
    public <D> D filterDestination(D destinationValue, Type<?> sourceType, String sourceName, Type<D> destType, String destName, MappingContext mappingContext);
    
    /**
     * Filters the input value
     * 
     * @param sourceValue
     * @param sourceType
     * @param sourceName
     * @param destType
     * @param destName
     * @param mappingContext the current mapping context
     * @return the filtered input value
     */
    public <S> S filterSource(S sourceValue, Type<S> sourceType, String sourceName, Type<?> destType, String destName, MappingContext mappingContext);
    
    
}
