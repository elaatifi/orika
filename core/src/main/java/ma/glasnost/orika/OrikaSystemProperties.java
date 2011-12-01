package ma.glasnost.orika;

/**
 * A hook for the various system properties which may be used to configure
 * Orika's runtime behavior.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public final class OrikaSystemProperties {

    private OrikaSystemProperties() {
	// prevent instantiation
    }
    
    /**
     * Specifies whether .java source files should be written for generated objects; 
     * valid choices are "true" or "false". <br>
     * Any non-null value will be evaluated to "false"; if this property is not specified,
     * default behavior is determined by the compiler strategy.
     */
    public static final String WRITE_SOURCE_FILES = "ma.glasnost.orika.writeSourceFiles";
    
    /**
     * Specifies whether class files should be written for generated objects; 
     * valid choices are "true" or "false". <br>
     * Any non-null value will be evaluated to "false"; if this property is not specified,
     * default behavior is determined by the compiler strategy.
     */
    public static final String WRITE_CLASS_FILES = "ma.glasnost.orika.writeClassFiles";
    
    /**
     * Specifies the fully-qualified class name of the compiler strategy to use when creating generated objects;
     * default value is determined by the MapperFactory implementation.
     */
    public static final String COMPILER_STRATEGY = "ma.glasnost.orika.compilerStrategy";
    
    /**
     * Specifies the fully-qualified class name of the un-enhancement strategy to use when performing type lookup 
     * in order to map objects; 
     * default value is determined by the MapperFactory implementation.
     */
    public static final String UNENHANCE_STRATEGY = "ma.glasnost.orika.unenhanceStrategy";
    
    /**
     * Specifies the fully-qualified class name of the constructor-resolver strategy to use when resolving 
     * constructors for instantiation of target types; 
     * default value is determined by the MapperFactory implementation.
     */
    public static final String CONSTRUCTOR_RESOLVER_STRATEGY = "ma.glasnost.orika.constructorResolverStrategy";
    
    /**
     * Specifies the fully-qualified class name of the converter factory to use when generating converters
     * for target types;  
     * default value is determined by the MapperFactory implementation.
     */
    public static final String CONVERTER_FACTORY = "ma.glasnost.orika.converterFactory";
    
}
