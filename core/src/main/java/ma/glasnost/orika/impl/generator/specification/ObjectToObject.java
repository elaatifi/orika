package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class ObjectToObject extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return true;
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        String mapNewObject = destination.assign(format("(%s)%s(%s, mappingContext)", destination.typeName(), code.usedMapperFacadeCall(source, destination), source));
        String mapExistingObject = destination.assign(format("(%s)%s(%s, %s, mappingContext)", destination.typeName(), code.usedMapperFacadeCall(source, destination), source, destination));
        String mapStmt = format(" %s { %s; } else { %s; }", destination.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (fieldMap.getInverse() != null) {
            VariableRef inverse = new VariableRef(fieldMap.getInverse(), destination);
            
            if (inverse.isCollection()) {
                MultiOccurrenceVariableRef inverseCollection = MultiOccurrenceVariableRef.from(inverse);
                ipStmt += inverse.ifNull() + inverse.assign(inverseCollection.newCollection()) + ";";
                ipStmt += format("%s.add(%s);", inverse, destination.owner());
            } else if (inverse.isArray()) {
                ipStmt += "/* TODO Orika source code does not support Arrays */";
            } else {
                ipStmt += statement(inverse.assign(destination.owner()));
            }
        }
        
        String mapNull = code.shouldMapNulls() ? format(" else {\n %s;\n}\n", destination.assign("null")): "";
        return statement("%s { %s;  %s } %s", source.ifNotNull(), mapStmt, ipStmt, mapNull);
        
    }
    
}
