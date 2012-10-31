package ma.glasnost.orika.impl.generator;

import java.util.List;

import ma.glasnost.orika.metadata.FieldMap;

/**
 * Specification encapsulates the logic to generate code for mapping
 * comparing a pair of types
 * 
 * @author mattdeboer
 *
 */
public interface AggregateSpecification {
    
    /**
     * Tests whether this Specification applies to the specified MappedTypePair
     * @param fieldMap 
     * 
     * @param typePair
     * @return true if this specification applies to the given MappedTypePair
     */
    boolean appliesTo(FieldMap fieldMap);
    
    
    /**
     * @param fieldMappings
     * @param code
     * @return
     */
    String generateMappingCode(List<FieldMap> fieldMappings, SourceCodeContext code);
}
