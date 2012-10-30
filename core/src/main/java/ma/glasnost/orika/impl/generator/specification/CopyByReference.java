package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class CopyByReference extends AbstractSpecification {

    public CopyByReference(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return ClassUtil.isImmutable(fieldMap.getSource().getType())
                && fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        if (source.type().isPrimitive() || source.type().isPrimitiveWrapper()) {
            return source + " == " + destination;
        } else {
            return source + ".equals(" + destination + ")";
        }
        
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return statement(destination.assign(source));
    }
    
}
