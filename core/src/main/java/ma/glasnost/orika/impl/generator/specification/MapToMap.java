package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

import java.util.Map;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.MapEntryRef;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.FieldMapBuilder;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.TypeFactory;

public class MapToMap extends AbstractSpecification {

    public MapToMap(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().isMap() && fieldMap.getDestination().isMap();
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        return source + " == " + destination;
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCodeContext code) {
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        
        StringBuilder out = new StringBuilder();
        
        out.append(s.ifNotNull());
        out.append("{\n");
        
        if (d.isAssignable()) {
            out.append(statement("if (%s == null) %s", d, d.assign(d.newMap())));
        }
        
        out.append(statement("%s.clear()", d));
        if (d.mapKeyType().equals(s.mapKeyType()) && d.mapValueType().equals(s.mapValueType())) {
            /*
             * Simple map-to-map case: both key and value types are identical
             */
            out.append(statement("%s.putAll(mapperFacade.mapAsMap(%s, %s, %s, mappingContext));", d, s, code.usedType(s.type()), code.usedType(d.type())));
        } else {
            VariableRef newKey = new VariableRef(d.mapKeyType(), "_$_key");
            VariableRef newVal = new VariableRef(d.mapValueType(), "_$_val");
            VariableRef entry = new VariableRef(TypeFactory.valueOf(Map.Entry.class), "_$_entry");
            VariableRef sourceKey = new MapEntryRef(s.mapKeyType(), "_$_entry", EntryPart.KEY);
            VariableRef sourceVal = new MapEntryRef(s.mapValueType(), "_$_entry", EntryPart.VALUE);
            /*
             * Loop through the individual entries, map key/value and then put
             * them into the destination
             */
            append(out,
                    format("for( Object _$_o: %s.entrySet()) {\n", s),
                    entry.declare("_$_o"),
                    newKey.declare(),
                    newVal.declare(),
                    code.mapFields(FieldMapBuilder.mapKeys(s.mapKeyType(), d.mapKeyType()), sourceKey, newKey, null, null),
                    code.mapFields(FieldMapBuilder.mapValues(s.mapValueType(), d.mapValueType()), sourceVal, newVal, null, null),
                    format("%s.put(%s, %s)", d, newKey, newVal),
                    "}\n");
        }
        
        String mapNull = code.shouldMapNulls() ? format(" else {\n %s;\n}", d.assignIfPossible("null")): "";
        append(out, "}" + mapNull);
        
        return out.toString();
    }
    
}
