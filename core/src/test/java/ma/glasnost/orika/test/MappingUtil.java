package ma.glasnost.orika.test;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public abstract class MappingUtil {

	static MapperFactory mapperFactory = new DefaultMapperFactory();

	public static MapperFactory getMapperFactory() {

		return mapperFactory;
	}
}
