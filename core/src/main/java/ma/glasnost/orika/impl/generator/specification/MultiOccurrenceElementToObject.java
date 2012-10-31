package ma.glasnost.orika.impl.generator.specification;


import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class MultiOccurrenceElementToObject extends AbstractSpecification {

    public MultiOccurrenceElementToObject(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isMapKey() || fieldMap.getSource().isArrayElement() || fieldMap.getSource().isListElement())
                && (ClassUtil.isImmutable(fieldMap.getDestination().getType()) || (!fieldMap.getDestination().isCollection()
                        && !fieldMap.getDestination().isArray() && !fieldMap.getDestination().isMap() && !fieldMap.getDestination()
                        .isEnum()));
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return statement(destination.assign(destination.cast(source)));
    }
}
