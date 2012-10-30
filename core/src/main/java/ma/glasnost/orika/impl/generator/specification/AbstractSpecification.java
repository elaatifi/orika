package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.Specification;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public abstract class AbstractSpecification implements Specification {

    protected MapperFactory mapperFactory;
    
    public AbstractSpecification(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
    
    public abstract boolean appliesTo(FieldMap fieldMap);

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        return source + ".equals(" + destination + ")";
    }

    public abstract String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code);
    
    
}
