package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;

public class ArrayOrCollectionToArray extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        final VariableRef arrayVar = destination.elementRef(destination.name());
        String newArray = format("%s[] %s = new %s[%s]", destination.elementTypeName(), destination.name(), destination.elementTypeName(), source.size());
        String mapArray;
        if (destination.elementType().isPrimitive()) {
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar, source, arrayVar.typeName());
        } else {
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", destination.name(), source, code.usedType(source.elementType()),
                    code.usedType(destination.elementType()));
        }
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else { %s; }", destination.assign("null")) : "";
        return format(" %s { %s; %s; %s; } %s", source.ifNotNull(), newArray, mapArray, destination.assign(arrayVar), mapNull);
    }
    
}
