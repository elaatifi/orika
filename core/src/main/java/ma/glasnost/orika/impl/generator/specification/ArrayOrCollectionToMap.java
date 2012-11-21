package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.append;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.statement;

import java.util.Map;

import ma.glasnost.orika.MapEntry;
import ma.glasnost.orika.impl.generator.MapEntryRef;
import ma.glasnost.orika.impl.generator.MapEntryRef.EntryPart;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Type;

public class ArrayOrCollectionToMap extends AbstractSpecification {

    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getDestination().isMap() && (fieldMap.getSource().isCollection() || fieldMap.getSource().isArray());
    }

    public String generateEqualityTestCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        // TODO:
        return "";
    }

    public String generateMappingCode(FieldMap fieldMap, VariableRef source, VariableRef destination, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        
        append(out,
                s.ifNotNull() + " {");
        
        if (d.isAssignable()) {
            out.append(statement("if (%s == null) %s", d, d.assign(d.newMap())));
        }
        out.append(statement("%s.clear()", d));
        
        VariableRef element = new VariableRef(s.elementType(), "_$_element");
        
        @SuppressWarnings("unchecked")
        Type<MapEntry<Object, Object>> entryType = MapEntry.concreteEntryType((Type<? extends Map<Object, Object>>) d.type());
        
        VariableRef newEntry = new VariableRef(entryType, "_$_entry");
        VariableRef newKey = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.KEY);
        VariableRef newVal = new MapEntryRef(newEntry.type(), newEntry.name(), EntryPart.VALUE);
        /*
         * Loop through the individual entries, map key/value and then put them
         * into the destination
         */
        append(out,
                format("for( Object _o : %s) {", s),
                element.declare("_o"),
                newEntry.declare("mapperFacade.map(%s, %s, %s, mappingContext)", element, code.usedType(element), code.usedType(newEntry)),
                "\n",
                format("%s.put(%s, %s)", d, newKey, newVal),
                "}");
        
        String mapNull = shouldMapNulls(fieldMap, code) ? format(" else {\n %s;\n}", d.assignIfPossible("null")): "";
        
        append(out, "}" + mapNull);
        
        return out.toString();
    }
    
}
