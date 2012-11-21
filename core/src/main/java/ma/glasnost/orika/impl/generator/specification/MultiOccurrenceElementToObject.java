package ma.glasnost.orika.impl.generator.specification;


import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;

public class MultiOccurrenceElementToObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isMapKey() || fieldMap.getSource().isArrayElement() || fieldMap.getSource().isListElement())
                && (ClassUtil.isImmutable(fieldMap.getDestination().getType()) || (!fieldMap.getDestination().isCollection()
                        && !fieldMap.getDestination().isArray() && !fieldMap.getDestination().isMap() && !fieldMap.getDestination()
                        .isEnum()));
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return statement(destination.assign(destination.cast(source)));
    }
}
