package ma.glasnost.orika.impl.generator.specification;

import static java.lang.String.format;
import static ma.glasnost.orika.impl.generator.SourceCodeContext.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.generator.AggregateSpecification;
import ma.glasnost.orika.impl.generator.Node;
import ma.glasnost.orika.impl.generator.Node.NodeList;
import ma.glasnost.orika.impl.generator.SourceCodeContext;
import ma.glasnost.orika.impl.generator.VariableRef;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;

/**
 * @author mattdeboer
 *
 */
public class MultiOccurrenceToMultiOccurrence implements AggregateSpecification {
    
    protected MapperFactory mapperFactory;
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * 
     * @param fieldMappings
     *            the field mappings to be applied
     * @param code 
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> SourceCodeBuilder
     */
    public String fromMultiOccurrenceToMultiOccurrence(List<FieldMap> fieldMappings, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        while (!fieldMappings.isEmpty()) {
            Set<FieldMap> associated = code.getAssociatedMappings(fieldMappings, fieldMappings.get(0));
            fieldMappings.removeAll(associated);
            
            NodeList sourceNodes = new NodeList();
            NodeList destNodes = new NodeList();
            
            for (FieldMap map : associated) {

                Node.addFieldMap(map, sourceNodes, true);
                Node.addFieldMap(map, destNodes, false);
            }    
              
            registerClassMaps(sourceNodes, destNodes);
            
            out.append(generateMultiOccurrenceMapping(sourceNodes, destNodes, associated, code));
        }
        return out.toString();
    }
    
    /**
     * Generates the code to support a (potentially parallel) mapping from one
     * or more multi-occurrence fields in the source type to one or more
     * multi-occurrence fields in the destination type.
     * @param sourceNodes 
     * @param destNodes 
     * 
     * @param sources
     *            the associated source variables
     * @param destinations
     *            the associated destination variables
     * @param subFields
     *            the nested properties of the individual field maps
     * @param code 
     * @param logDetails
     *            a StringBuilder to accept debug logging information
     * @return a reference to <code>this</code> CodeSourceBuilder
     */
    public String generateMultiOccurrenceMapping(NodeList sourceNodes, NodeList destNodes,
            Set<FieldMap> subFields, SourceCodeContext code) {
        
        StringBuilder out = new StringBuilder();
        
        /*
         * Construct size/length expressions used to limit the parallel iteration
         * of multiple source variables; only keep iterating so long as all variables
         * in a parallel set are non-empty
         */
        List<String> sourceSizes = new ArrayList<String>();
        for (Node ref : sourceNodes) {
            if (!ref.isLeaf()) {
                sourceSizes.add(ref.multiOccurrenceVar.size());
            }
        }
        
        String sizeExpr = join(sourceSizes, ", ");
        if (!"".equals(sizeExpr)) {
            sizeExpr = "min(new int[]{" + sizeExpr + "})";
        }
        
        /*
         * Declare "collector" elements and their iterators; used for aggregating
         * results which are finally assigned/copied back into their final destination(s)
         */
        for (Node destRef : destNodes) {
            
            if (!destRef.isLeaf()) {
                out.append(statement(destRef.newDestination.declare(destRef.newDestination.newInstance(sizeExpr))));
                if (destRef.newDestination.isArray()) {
                    out.append(statement(destRef.newDestination.declareIterator()));
                }
                List<Node> children = new ArrayList<Node>();
                children.add(destRef);
                while (!children.isEmpty()) {
                    Node child = children.remove(0);
                    children.addAll(child.children);
                    if (child.elementRef != null) {
                        out.append(statement(child.elementRef.declare()));
                        if (child.multiOccurrenceVar.isArray()) {
                            out.append(statement(child.multiOccurrenceVar.declareIterator()));
                        }
                        if (child.elementRef.isPrimitive()) {
                            out.append(statement(child.nullCheck.declare("true")));
                        }
                    }
                }
            }
        }
        
        StringBuilder endWhiles = new StringBuilder();
        StringBuilder addLastElement = new StringBuilder();
        
        iterateSources(sourceNodes, destNodes, out, endWhiles);
        
        LinkedList<Node> stack = new LinkedList<Node>(destNodes);
        while (!stack.isEmpty()) {
            
            Node currentNode = stack.removeFirst();
            stack.addAll(0, currentNode.children);
            Node srcNode = null;
            if (currentNode.value != null) {
                srcNode = Node.findFieldMap(currentNode.value, sourceNodes, true);
            } else {
                FieldMap fieldMap = currentNode.getMap();
                if (fieldMap != null) {
                    srcNode = Node.findFieldMap(fieldMap, sourceNodes, true).parent;
                }
            }
            
            if (!currentNode.isLeaf() && srcNode != null) { 
                
                String currentElementNull = currentNode.elementRef.isPrimitive() ? currentNode.nullCheck.toString() : currentNode.elementRef.isNull();
                String currentElementComparator = code.currentElementComparator(srcNode, currentNode, sourceNodes, destNodes);
                String or = (!"".equals(currentElementNull) && !"".equals(currentElementComparator)) ? " || " : "";
                
                /*append(out,
                        "if ( " + currentElementNull + or + currentElementComparator + ") {\n",
                        "if ( !(" + currentElementNull + ")) {\n",
                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
                        "}\n",
                        currentNode.elementRef.assign(code.newObject(srcNode.elementRef, currentNode.elementRef.type())),
                        (currentNode.elementRef.isPrimitive() ? currentNode.nullCheck.assign("false") : ""),
                        "}");*/
                
                append(out,
                        "if ( " + currentElementNull + or + currentElementComparator + ") {\n",
//                        "if ( !(" + currentElementNull + ")) {\n",
//                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
//                        "}\n",
                        currentNode.elementRef.assign(code.newObject(srcNode.elementRef, currentNode.elementRef.type())),
                        (currentNode.elementRef.isPrimitive() ? currentNode.nullCheck.assign("false") : ""),
                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
                        "}");
                
//                append(addLastElement,
//                        "if ( !(" + currentElementNull + ")) {\n",
//                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
//                        "}\n");
            }
            
            if (currentNode.value != null) {
                    
                /*
                 * If we have a fieldMap for the current node, attempt to map the fields
                 */
                String srcName = srcNode.parent != null ? srcNode.parent.elementRef.name() : "source";
                Property srcProp = new Property.Builder().merge(currentNode.value.getSource()).expression(currentNode.value.getSource().getName()).build();
                VariableRef s = new VariableRef(srcProp, srcName);
                
                Property dstProp = new Property.Builder().merge(currentNode.value.getDestination()).expression(currentNode.value.getDestination().getExpression()).build();
                String dstName =  "destination";
                if (currentNode.parent != null ) {
                    dstName = currentNode.parent.elementRef.name();
                }
                
                VariableRef d = new VariableRef(dstProp, dstName);
                
                Type<?> destType = currentNode.parent != null ? currentNode.parent.elementRef.type() : null;
                
                out.append(statement(code.mapFields(currentNode.value, s, d, destType, null)));
            }
        }  
        
        out.append(endWhiles.toString());
        out.append(addLastElement.toString());
        
        /*
         * Finally, we loop over the destination nodes and assign/copy all of the temporary 
         * "collector" variables back into their final destination
         */
        for (Node destRef : destNodes) {
            if (destRef.isRoot() && !destRef.isLeaf()) {
                if (destRef.multiOccurrenceVar.isArray() || destRef.multiOccurrenceVar.isMap()) {
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                } else {
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            format("if (%s) {", destRef.multiOccurrenceVar.isNull()),
                            destRef.multiOccurrenceVar.assignIfPossible(destRef.multiOccurrenceVar.newInstance(sizeExpr)),
                            "} else {\n",
                            destRef.multiOccurrenceVar + ".clear()",
                            "}\n",
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                }
            }
        }
        
        return out.toString();
    }
    
    /**
     * Register the ClassMaps needed to map this pair of source and
     * destination nodes.
     * 
     * @param sourceNodes
     * @param destNodes
     */
    private void registerClassMaps(NodeList sourceNodes, NodeList destNodes) {
        /*
         * Register all of the subordinate ClassMaps needed by this multi-occurrence
         * mapping 
         */
        Map<MapperKey, ClassMapBuilder<?,?>> builders = new HashMap<MapperKey, ClassMapBuilder<?,?>>();
        
        LinkedList<Node> stack = new LinkedList<Node>(destNodes);
        while (!stack.isEmpty()) {
            
            Node currentNode = stack.removeFirst();
            stack.addAll(0, currentNode.children);
            Node srcNode = null;
            if (currentNode.value != null) {
                srcNode = Node.findFieldMap(currentNode.value, sourceNodes, true);
            } else {
                FieldMap fieldMap = currentNode.getMap();
                if (fieldMap != null) {
                    srcNode = Node.findFieldMap(fieldMap, sourceNodes, true).parent;
                }
            }
        
            if (srcNode.parent != null 
                    && srcNode.parent.elementRef != null 
                    && currentNode.parent != null 
                    && currentNode.parent.elementRef != null) {
                
                MapperKey key = new MapperKey(srcNode.parent.elementRef.type(), currentNode.parent.elementRef.type());
                if (!ClassUtil.isImmutable(key.getAType()) 
                        && !ClassUtil.isImmutable(key.getBType()) 
                        && !mapperFactory.existsRegisteredMapper(key.getAType(), key.getBType(), true)) {
                    ClassMapBuilder<?,?> builder = builders.get(key);
                    if (builder == null) {
                        builder = mapperFactory.classMap(key.getAType(), key.getBType());
                        builders.put(key, builder);
                    }
                    builder.fieldMap(currentNode.value.getSource().getName(), currentNode.value.getDestination().getName()).add();
                }
            }
        }
        
        
        for (ClassMapBuilder<?,?> builder: builders.values()) {
            builder.register();
        }
    }
    
    /**
     * Creates the looping constructs for nested source variables
     * 
     * @param sourceNodes
     * @param destNodes
     * @param out
     * @param endWhiles
     */
    private void iterateSources(NodeList sourceNodes, NodeList destNodes, StringBuilder out, StringBuilder endWhiles) {
        
        if (!sourceNodes.isEmpty()) {
            for (Node srcRef : sourceNodes) {
                if (!srcRef.isLeaf()) {
                    out.append(statement(srcRef.multiOccurrenceVar.declareIterator()));
                }
            }
            
            StringBuilder loopSource = new StringBuilder();
            /*
             * Create while loop for the top level multi-occurrence objects
             */
            loopSource.append("while (");
            Iterator<Node> sourcesIter = sourceNodes.iterator();
            boolean atLeastOneIter = false;
            while (sourcesIter.hasNext()) {
                Node ref = sourcesIter.next();
                if (!ref.isLeaf()) {
                    if (atLeastOneIter) {
                        loopSource.append(" && ");
                    }
                    loopSource.append(ref.multiOccurrenceVar.iteratorHasNext());
                    atLeastOneIter = true;
                }
            }
            loopSource.append(") {");
            
            if (atLeastOneIter) {
                out.append("\n");
                out.append(loopSource.toString());
            }
            for (Node srcRef : sourceNodes) {
                
                if (!srcRef.isLeaf()) {
                    out.append(statement(srcRef.elementRef.declare(srcRef.multiOccurrenceVar.nextElement())));
                    iterateSources(srcRef.children, destNodes, out, endWhiles);
                }
            }
            if (atLeastOneIter) {
                endWhiles.append("}\n");
            }
        }
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#appliesTo(ma.glasnost.orika.metadata.FieldMap)
     */
    public boolean appliesTo(FieldMap fieldMap) {
        return fieldMap.getSource().getContainer() != null || fieldMap.getDestination().getContainer() != null;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#generateMappingCode(java.util.Set, ma.glasnost.orika.impl.generator.SourceCode)
     */
    public String generateMappingCode(List<FieldMap> fieldMappings, SourceCodeContext code) {
        return this.fromMultiOccurrenceToMultiOccurrence(fieldMappings, code);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.impl.generator.AggregateSpecification#setMapperFactory(ma.glasnost.orika.MapperFactory)
     */
    public void setMapperFactory(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
}
