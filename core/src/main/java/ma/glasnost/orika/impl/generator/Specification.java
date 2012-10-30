package ma.glasnost.orika.impl.generator;

import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

/**
 * Specification encapsulates the logic to generate code for mapping
 * comparing a pair of types
 * 
 * @author mattdeboer
 *
 */
public interface Specification {
    
    /**
     * Tests whether this Specification applies to the specified MappedTypePair
     * @param fieldMap 
     * 
     * @param typePair
     * @return true if this specification applies to the given MappedTypePair
     */
    boolean appliesTo(FieldMap fieldMap);
    
    
    /**
     * Generates code for a boolean equality test between the two variable types,
     * where are potentially unrelated.
     * 
     * @param source
     * @param destination
     * @param inverseProperty 
     * @param code 
     * @return the code snippet which represents a true|false statement describing
     * whether the two types should be considered 'equal'
     */
    String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code);
    
    
    /**
     * Generates code for
     * 
     * @param source
     * @param destination
     * @param inverseProperty 
     * @param code 
     * @return the code snippet which represents mapping from the source to destination
     * property
     */
    String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code);
}
