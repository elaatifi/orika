package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class PrimitiveOrWrapperToPrimitive extends AbstractSpecification {

    public PrimitiveOrWrapperToPrimitive(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isPrimitive() && fieldMap.getSource().getType().isPrimitiveWrapper();
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        if (source.isPrimitive()) {
            return source + " == " + destination;
        } else {
            return "(" + source.notNull() + " && " +  source + " == " + destination + ")";
        }
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        if (source.isPrimitive()) {
            return statement(destination.assign(source));
        } else {
            return statement(source.ifNotNull() + destination.assign(source));
        }
    }
    
}
