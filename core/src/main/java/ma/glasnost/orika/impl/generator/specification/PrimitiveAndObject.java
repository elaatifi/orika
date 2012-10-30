package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

/**
 * @author mattdeboer
 *
 */
public class PrimitiveAndObject extends AbstractSpecification {

    public PrimitiveAndObject(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().getType().isPrimitive() || fieldMap.getDestination().getType().isPrimitive();
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        throw new MappingException("Encountered mapping of primitive to object (or vise-versa); sourceType="+
                source.type() + ", destinationType=" + destination.type());
    }
    
}
