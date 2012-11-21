package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;

public class ObjectToMultiOccurrenceElement extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getDestination().isMapKey() || fieldMap.getDestination().isArrayElement() || fieldMap.getDestination().isListElement())
                && (ClassUtil.isImmutable(fieldMap.getSource().getType()) || (!fieldMap.getSource().isCollection()
                        && !fieldMap.getSource().isArray() && !fieldMap.getSource().isMap() && !fieldMap.getSource()
                        .isEnum()));
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        if (!source.isPrimitive()) {
            out.append(source.ifNotNull() + "{");
        }
        out.append(statement(destination.assign(source)));
        if (!source.isPrimitive()) {
            out.append("}");
            if (shouldMapNulls(fieldMap, code) && !destination.isPrimitive()) {
                append(out, 
                        " else {\n",
                        destination.assignIfPossible("null"),
                        "}\n");
            }
        }
        return out.toString();
    }

}
