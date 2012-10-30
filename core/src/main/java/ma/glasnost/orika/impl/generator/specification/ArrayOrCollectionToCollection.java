package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCode.append;
import static ma.glasnost.orika.impl.generator.SourceCode.statement;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.impl.generator.MultiOccurrenceVariableRef;
import ma.glasnost.orika.impl.generator.SourceCode;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;

public class ArrayOrCollectionToCollection extends AbstractSpecification {

    public ArrayOrCollectionToCollection(MapperFactory mapperFactory) {
        super(mapperFactory);
    }

    public boolean appliesTo(FieldMap fieldMap) {
        return (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection()) && fieldMap.getDestination().isCollection();
    }

    public String generateEqualityTestCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        // TODO:
        return "";
    }

    public String generateMappingCode(VariableRef source, VariableRef destination, Property inverseProperty, SourceCode code) {
        
        StringBuilder out = new StringBuilder();
        
        MultiOccurrenceVariableRef s = MultiOccurrenceVariableRef.from(source);
        MultiOccurrenceVariableRef d = MultiOccurrenceVariableRef.from(destination);
        
        final Class<?> dc = destination.getOwner().rawType();
        
        final Class<?> destinationElementClass = d.elementType().getRawType();
        
        if (destinationElementClass == null) {
            throw new MappingException("cannot determine runtime type of destination collection " + dc.getName() + "." + d.name());
        }
        
        // Start check if source property ! = null
        out.append(s.ifNotNull() + " {\n");
        
        if (d.isAssignable()) {
            out.append(statement("if (%s == null) %s", d, d.assign(d.newInstance(source.size()))));
        }
        
        if (s.isArray()) {
            if (s.elementType().isPrimitive()) {
                out.append("\n");
                out.append(statement("%s.addAll(asList(%s));", d, s));
            } else {
                out.append("\n");
                out.append(statement("%s.addAll(mapperFacade.mapAsList(asList(%s), %s.class, mappingContext));", d, s, d.typeName()));
            }
        } else {
            append(out,
                    "\n",
                    format("%s.clear()", d),
                    "\n",
                    format("%s.addAll(mapperFacade.mapAs%s(%s, %s, %s, mappingContext))", d, d.collectionType(), s,
                            code.usedType(s.elementType()), code.usedType(d.elementType())));
        }
        if (inverseProperty != null) {
            final MultiOccurrenceVariableRef inverse = new MultiOccurrenceVariableRef(inverseProperty, "orikaCollectionItem");
            
            if (inverseProperty.isCollection()) {
                append(out,
                          format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d),
                          format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()),
                          format("    %s { %s; }", inverse.ifNull(), inverse.assignIfPossible(inverse.newCollection())),
                          format("    %s.add(%s)", inverse, d.owner()),
                          "}");
                
            } else if (inverseProperty.isArray()) {
                out.append(" // TODO support array");
            } else {
                append(out,
                        format("for (java.util.Iterator orikaIterator = %s.iterator(); orikaIterator.hasNext();) { ", d),
                        format("    %s orikaCollectionItem = (%s) orikaIterator.next();", d.elementTypeName(), d.elementTypeName()),
                        inverse.assign(d.owner()),
                        "}");
                
            }
        }
        // End check if source property ! = null
        append(out,
                "} else { ",
                d.assignIfPossible("null"),
                "}\n");
        
        return out.toString();
    }
    
}
