package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class AnyTypeToString extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return String.class.equals(fieldMap.getDestination().getType().getRawType());
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return "(\"\" + " + source + ").equals(" + destination +")";
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        if (source.isPrimitive()) {
            return statement(destination.assign("\"\"+ %s", source));
        } else {
            return statement(source.ifNotNull() + destination.assign("%s.toString()", source));
        }
    }
}
