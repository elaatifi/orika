package ma.glasnost.orika.test.community;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Test;

public class Issue18TestCase {

	
	@SuppressWarnings("deprecation")
	@Test
	public void testMappingEmptyArray() {
		
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
		mapperFactory.registerClassMap(ClassMapBuilder.map(Object.class,Object.class).byDefault().toClassMap());
		List<Object> listA = new ArrayList<Object>();
		List<Object> listB = mapperFactory.getMapperFacade().mapAsList(listA, Object.class);
		
		Assert.assertNotNull(listB);
		Assert.assertTrue(listB.isEmpty());
	}
	
}
