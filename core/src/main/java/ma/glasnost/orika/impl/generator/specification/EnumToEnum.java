package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
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
    
    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return "(Enum.valueOf(%s.class, %s.name()) == " + destination + ")";
    }
    
    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        String assignEnum = destination.assign("Enum.valueOf(%s.class, %s.name())", destination.typeName(), source);
        String mapNull = code.shouldMapNulls() ? format(" else {\n %s;\n}", destination.assignIfPossible("null")): "";
        return statement("%s { %s; } %s", source.ifNotNull(), assignEnum, mapNull);
    }
}


