package ma.glasnost.orika.test.community;

import java.util.Collection;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Test;

public class EmptyCollectionToArrayTestCase {

	public static class A {
	       private Collection<String>      list;

	       public void setList(final Collection<String> list) {
	               this.list = list;
	       }

	       public Collection<String> getList() {
	               return list;
	       }
	}

	public static class B {
	       private String[]        list;

	       public void setList(final String[] list) {
	               this.list = list;
	       }

	       public String[] getList() {
	               return list;
	       }
	}

	
	@Test
	public void testMappingListTypes() {
		final MapperFactory mapperFactory = new	DefaultMapperFactory.Builder().build();
		final ClassMapBuilder<A, B> builder = ClassMapBuilder.map(A.class, B.class);
	
		mapperFactory.registerClassMap(builder.byDefault().toClassMap());
		
		final MapperFacade facade = mapperFactory.getMapperFacade();
		
		final A a = new A();
		final B converted = facade.map(a, B.class);
		
		Assert.assertNotNull(converted);
		Assert.assertNull(converted.getList());
	}

	
}
