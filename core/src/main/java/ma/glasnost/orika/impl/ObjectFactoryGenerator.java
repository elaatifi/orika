package ma.glasnost.orika.impl;

import static ma.glasnost.orika.impl.Specifications.aCollection;
import static ma.glasnost.orika.impl.Specifications.aPrimitiveToWrapper;
import static ma.glasnost.orika.impl.Specifications.aWrapperToPrimitive;
import static ma.glasnost.orika.impl.Specifications.anArray;
import static ma.glasnost.orika.impl.Specifications.immutable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.constructor.ConstructorResolverStrategy;
import ma.glasnost.orika.converter.Converter;
import ma.glasnost.orika.converter.ConverterFactory;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.MapperKey;
import ma.glasnost.orika.metadata.Property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.paranamer.AdaptiveParanamer;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

public class ObjectFactoryGenerator {
    
    private final static Logger LOG = LoggerFactory.getLogger(ObjectFactoryGenerator.class);
    
    private final ConstructorResolverStrategy constructorResolverStrategy;
    private final MapperFactory mapperFactory;
    private final ClassPool classPool;
    private final Paranamer paranamer;
    
    public ObjectFactoryGenerator(MapperFactory mapperFactory, ConstructorResolverStrategy constructorResolverStrategy) {
        this.mapperFactory = mapperFactory;
        this.classPool = ClassPool.getDefault();
        this.paranamer = new CachingParanamer(new AdaptiveParanamer(new BytecodeReadingParanamer(), new AnnotationParanamer()));
        
        classPool.insertClassPath(new ClassClassPath(this.getClass()));
        
        this.constructorResolverStrategy = constructorResolverStrategy;
    }
    
    public GeneratedObjectFactory build(Class<Object> clazz) {
        
        final String className = clazz.getSimpleName() + "ObjectFactory" + System.identityHashCode(clazz);
        
        try {
            final GeneratedSourceCode factoryCode = new GeneratedSourceCode(className, classPool, GeneratedObjectFactory.class);
            
            addCreateMethod(factoryCode, clazz);
            
            GeneratedObjectFactory objectFactory = (GeneratedObjectFactory) factoryCode.getInstance();
            objectFactory.setMapperFacade(mapperFactory.getMapperFacade());
            
            return objectFactory;
            
        } catch (final Exception e) {
            throw new MappingException(e);
        }
    }
    
    private void addCreateMethod(GeneratedSourceCode context, Class<Object> clazz) throws CannotCompileException {
        final CodeSourceBuilder out = new CodeSourceBuilder(1);
        out.append("public Object create(Object s) {");
        out.append("if(s == null) throw new %s(\"[s] must be not null\");", IllegalArgumentException.class.getName());
        
        Set<Class<Object>> sourceClasses = mapperFactory.lookupMappedClasses(clazz);
        
        if (sourceClasses != null && !sourceClasses.isEmpty()) {
            for (Class<Object> sourceClass : sourceClasses) {
                addSourceClassConstructor(out, clazz, sourceClass);
            }
        }
        out.append("throw new %s(\"[s] is an unsupported source class : \"+s.getClass().getName());",
                IllegalArgumentException.class.getName());
        out.append("\n}");
        
        try {
            context.addMethod(out.toString());
        } catch (final CannotCompileException e) {
            LOG.error("An exception occured while compiling: " + out.toString(), e);
            throw e;
        }
    }
    
    private void addSourceClassConstructor(CodeSourceBuilder out, Class<Object> clazz, Class<Object> sourceClass) {
        List<FieldMap> properties = new ArrayList<FieldMap>();
        ClassMap<Object, Object> classMap = mapperFactory.getClassMap(new MapperKey(clazz, sourceClass));
        boolean aToB = classMap.getBType().equals(clazz);
        
        try {
            Constructor<Object> constructor = constructorResolverStrategy.resolve(classMap, clazz);
            
            String[] parameters = paranamer.lookupParameterNames(constructor);
            Class<?>[] constructorArguments = constructor.getParameterTypes();
            
            // TODO need optimizations
            for (String param : parameters) {
                for (FieldMap fieldMap : classMap.getFieldsMapping()) {
                    if (!aToB)
                        fieldMap = fieldMap.flip();
                    if (param.equals(fieldMap.getDestination().getName())) {
                        properties.add(fieldMap);
                        break;
                    }
                }
            }
            
            if (parameters.length != properties.size()) {
                throw new MappingException("Can not find all constructor's parameters");
            }
            
            out.ifSourceInstanceOf(sourceClass).then();
            out.append("%s source = (%s) s;", sourceClass.getName(), sourceClass.getName());
            int argIndex = 0;
            for (FieldMap fieldMap : properties) {
                
                final Property sp = fieldMap.getSource(), dp = fieldMap.getDestination();
                String var = "arg" + argIndex;
                Class<?> targetClass = constructorArguments[argIndex];
                out.declareVar(targetClass, var);
                argIndex++;
                
                if (generateConverterCode(out, var, fieldMap)) {
                    continue;
                }
                try {
                    
                    if (fieldMap.is(immutable())) {
                        out.ifSourceNotNull(sp).assignImmutableVar(var, sp);
                    } else if (fieldMap.is(anArray())) {
                        out.ifSourceNotNull(sp).assignArrayVar(var, sp, targetClass);
                    } else if (fieldMap.is(aCollection())) {
                        out.ifSourceNotNull(sp).assignCollectionVar(var, sp, dp);
                    } else if (fieldMap.is(aWrapperToPrimitive())) {
                        out.ifSourceNotNull(sp).assignWrapperToPrimitiveVar(var, sp, targetClass);
                    } else if (fieldMap.is(aPrimitiveToWrapper())) {
                        out.ifSourceNotNull(sp).assignPrimtiveToWrapperVar(var, sp, targetClass);
                    } else { /**/
                        out.ifSourceNotNull(sp).then().assignObjectVar(var, sp, targetClass).end();
                    }
                    
                } catch (final Exception e) {
                }
            }
            
            out.append("return new %s", clazz.getName()).append("(");
            for (int i = 0; i < properties.size(); i++) {
                out.append("arg%d", i);
                if (i < properties.size() - 1) {
                    out.append(",");
                }
            }
            out.append(");").end();
            
        } catch (Exception e) {
            LOG.warn("Can not find " + clazz.getName() + "constructor's parameters name");
            /* SKIP */
        }
    }
    
    @SuppressWarnings("unchecked")
    private boolean generateConverterCode(final CodeSourceBuilder code, String var, FieldMap fieldMap) {
        Property sp = fieldMap.getSource(), dp = fieldMap.getDestination();
        final Class<?> destinationClass = dp.getType();
        
        Converter<Object, Object> converter = null;
        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
        if (fieldMap.getConverterId() != null) {
            converter = converterFactory.getConverter(fieldMap.getConverterId());
        } else {
            converter = converterFactory.getConverter((Class<Object>) sp.getType(), (Class<Object>) destinationClass);
        }
        
        if (converter != null) {
            code.ifSourceNotNull(sp).then().assignConvertedVar(var, sp, destinationClass, fieldMap.getConverterId()).end();
            return true;
        } else {
            return false;
        }
    }
}
