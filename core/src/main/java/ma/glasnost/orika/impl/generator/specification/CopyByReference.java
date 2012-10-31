package ma.glasnost.orika.impl.generator.specification;

import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class CopyByReference extends AbstractSpecification {

    public CopyByReference(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return ClassUtil.isImmutable(fieldMap.getSource().getType())
                && fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        if (source.type().isPrimitive() || source.type().isPrimitiveWrapper()) {
            return source + " == " + destination;
        } else {
            return source + ".equals(" + destination + ")";
        }
        
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        StringBuilder out = new StringBuilder();
        if (!source.isPrimitive()) {
            out.append(source.ifNotNull() + "{");
        }
        out.append(statement(destination.assign(source)));
        if (!source.isPrimitive()) {
            out.append("}");
            if (code.shouldMapNulls() && !destination.isPrimitive()) {
                append(out, 
                        " else {\n",
                        destination.assignIfPossible("null"),
                        "}\n");
            }
        }
        return out.toString();
    }
    
}
