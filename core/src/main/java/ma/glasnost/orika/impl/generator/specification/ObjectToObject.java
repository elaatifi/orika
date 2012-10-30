package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class ObjectToObject extends AbstractSpecification {

    public ObjectToObject(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return true;
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return "";
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        
        String mapNewObject = destination.assign(format("(%s)%s(%s, mappingContext)", destination.typeName(), code.usedMapperFacadeCall(source, destination), source));
        String mapExistingObject = format("%s(%s, %s, mappingContext)", code.usedMapperFacadeCall(source, destination), source, destination);
        String mapStmt = format(" %s { %s; } else { %s; }", destination.ifNull(), mapNewObject, mapExistingObject);
        
        String ipStmt = "";
        if (inverseProperty != null) {
            VariableRef inverse = new VariableRef(inverseProperty, destination);
            
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
        
        return statement("%s { %s;  %s } else { %s; }", source.ifNotNull(), mapStmt, ipStmt, destination.assign("null"));
        
    }
    
}
