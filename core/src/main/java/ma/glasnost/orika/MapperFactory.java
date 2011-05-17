package ma.glasnost.orika;

import ma.glasnost.orika.impl.GeneratedMapperBase;
import ma.glasnost.orika.impl.MappingContext;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.MapperKey;

public interface MapperFactory {

	GeneratedMapperBase get(MapperKey mapperKey);

	<S, D> void registerClassMap(ClassMap<S, D> classMap);

	<S, D> void registerConverter(String converterId, Converter<S, D> converter);

	<S, D> Converter<S, D> lookupConverter(Class<S> source, Class<D> destination);

	<T> void registerObjectFactory(ObjectFactory<T> objectFactory, Class<T> targetClass);

	<T> ObjectFactory<T> lookupObjectFactory(Class<T> targetClass);

	<S, D> Class<? extends D> lookupConcreteDestinationClass(Class<S> sourceClass, Class<D> destinationClass,
			MappingContext context);

	MapperFacade getMapperFacade();
}
