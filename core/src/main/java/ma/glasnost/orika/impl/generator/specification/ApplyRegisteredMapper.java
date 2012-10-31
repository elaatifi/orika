package ma.glasnost.orika.impl.generator.specification;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.FieldMap;

public class ApplyRegisteredMapper extends ObjectToObject {

    public ApplyRegisteredMapper(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return mapperFactory.existsRegisteredMapper(fieldMap.getAType(), fieldMap.getBType(), false);
    }
}
