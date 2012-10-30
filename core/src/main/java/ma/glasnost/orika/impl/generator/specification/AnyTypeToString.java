package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class AnyTypeToString extends AbstractSpecification {

    public AnyTypeToString(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return String.class.equals(fieldMap.getDestination().getType().getRawType());
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return "(\"\" + " + source + ").equals(" + destination +")";
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        if (source.isPrimitive()) {
            return statement(destination.assign("\"\"+ %s", source));
        } else {
            return statement(source.ifNotNull() + destination.assign("%s.toString()", source));
        }
    }
}
