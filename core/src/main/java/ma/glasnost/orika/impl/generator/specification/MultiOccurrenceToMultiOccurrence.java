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
    
    private final MapperFactory mapperFactory;
    
    /**
     * @param mapperFactory
     * @param propertyResolver
     */
    public MultiOccurrenceToMultiOccurrence(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
    
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
        
        Map<MapperKey, ClassMapBuilder<?,?>> builders = new HashMap<MapperKey, ClassMapBuilder<?,?>>();
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
                
                append(out,
                        "if ( " + currentElementNull + or + currentElementComparator + ") {\n",
                        "if ( !(" + currentElementNull + ")) {\n",
                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
                        "}\n",
                        currentNode.elementRef.assign(code.newObject(currentNode.elementRef, currentNode.elementRef.type())),
                        (currentNode.elementRef.isPrimitive() ? currentNode.nullCheck.assign("false") : ""),
                        "}");
                
                append(addLastElement,
                        "if ( !(" + currentElementNull + ")) {\n",
                        (currentNode.isRoot() ? currentNode.newDestination.add(currentNode.elementRef) : currentNode.multiOccurrenceVar.add(currentNode.elementRef)),
                        "}\n");
            }
            
            if (currentNode.value != null) {
                    
                    
                String srcName = srcNode.parent != null ? srcNode.parent.elementRef.name() : "source";
                Property srcProp = new Property.Builder().merge(currentNode.value.getSource()).expression(currentNode.value.getSource().getName()).build();
                VariableRef s = new VariableRef(srcProp, srcName);
                
                String dstName = currentNode.parent != null ? currentNode.parent.elementRef.name() : "destination";
                Property dstProp = new Property.Builder().merge(currentNode.value.getDestination()).expression(currentNode.value.getDestination().getName()).build();
                VariableRef d = new VariableRef(dstProp, dstName);
                
                Type<?> destType = currentNode.parent != null ? currentNode.parent.elementRef.type() : null;
                
                out.append(statement(code.mapFields(currentNode.value, s, d, destType, null)));
                    
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
        }  
        
        out.append(endWhiles.toString());
        out.append(addLastElement.toString());
        
        for (Node destRef : destNodes) {
            if (destRef.isRoot() && !destRef.isLeaf()) {
                if (destRef.multiOccurrenceVar.isArray()) {
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                } else {
                    append(out,
                            format("if (%s && %s) {",destRef.newDestination.notNull(), destRef.newDestination.notEmpty()),
                            format("if (%s) {", destRef.multiOccurrenceVar.isNull()),
                            destRef.multiOccurrenceVar.assign(destRef.multiOccurrenceVar.newInstance(sizeExpr)),
                            "} else {\n",
                            destRef.multiOccurrenceVar + ".clear()",
                            "}\n",
                            destRef.multiOccurrenceVar.addAll(destRef.newDestination),
                            "}\n");
                }
            }
        }
        
        for (ClassMapBuilder<?,?> builder: builders.values()) {
            builder.register();
        }
        
        return out.toString();
    }
    
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
}
