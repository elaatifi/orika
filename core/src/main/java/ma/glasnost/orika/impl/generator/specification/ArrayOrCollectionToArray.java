package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class ArrayOrCollectionToArray extends AbstractSpecification {

    public ArrayOrCollectionToArray(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection());
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return "";
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        
        final VariableRef arrayVar = destination.elementRef(destination.name());
        String newArray = format("%s[] %s = new %s[%s]", destination.elementTypeName(), destination.name(), destination.elementTypeName(), source.size());
        String mapArray;
        if (destination.elementType().isPrimitive()) {
            mapArray = format("mapArray(%s, asList(%s), %s.class, mappingContext)", arrayVar, source, arrayVar.typeName());
        } else {
            mapArray = format("mapperFacade.mapAsArray(%s, asList(%s), %s, %s, mappingContext)", destination.name(), source, code.usedType(source.elementType()),
                    code.usedType(destination.elementType()));
        }
        return format(" %s { %s; %s; %s; } else { %s; }", source.ifNotNull(), newArray, mapArray, destination.assign(arrayVar), destination.assign("null"));
    }
    
}
