package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MappedTypePair;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class ObjectToMultiOccurrenceElement extends AbstractSpecification {

    public ObjectToMultiOccurrenceElement(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getDestination().isMapKey() || fieldMap.getDestination().isArrayElement() || fieldMap.getDestination().isListElement())
                && (ClassUtil.isImmutable(fieldMap.getSource().getType()) || (!fieldMap.getSource().isCollection()
                        && !fieldMap.getSource().isArray() && !fieldMap.getSource().isMap() && !fieldMap.getSource()
                        .isEnum()));
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return statement(destination.assign(source));
    }

}
