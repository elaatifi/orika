package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class PrimitiveOrWrapperToPrimitive extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isPrimitive() && fieldMap.getSource().getType().isPrimitiveWrapper();
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        if (source.isPrimitive()) {
            return source + " == " + destination;
        } else {
            return "(" + source.notNull() + " && " +  source + " == " + destination + ")";
        }
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        if (source.isPrimitive()) {
            return statement(destination.assign(source));
        } else {
            return statement(source.ifNotNull() + destination.assign(source));
        }
    }
    
}
