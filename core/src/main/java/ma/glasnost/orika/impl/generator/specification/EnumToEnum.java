package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class EnumToEnum extends AbstractSpecification {
    
    public EnumToEnum(MapperFactory mapperFactory) {
        super(mapperFactory);
    }
    
    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getBType().isEnum() && fieldMap.getAType().isEnum();
    }
    
    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return "(Enum.valueOf(%s.class, %s.name()) == " + destination + ")";
    }
    
    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        String assignEnum = destination.assign("Enum.valueOf(%s.class, %s.name())", destination.typeName(), source);
        return statement("%s { %s; } else { %s; }", source.ifNotNull(), assignEnum, destination.assign("null"));
    }
}
