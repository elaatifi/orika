package ma.glasnost.orika.test.community.issue135;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

public class Issue135Test {

	private MapperFacade mapper;

	@Before
	public void setup() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy()).build();
		mapperFactory.classMap(Domain.class, Representation.class)
		.mapNulls(true)
		.mapNullsInReverse(true)
		.field("subA", "repA")
		.field("subB", "repA.repB") // this causes NPE if repA is null
		.byDefault()
		.register();

		mapper = mapperFactory.getMapperFacade();
	}

	@Test
	public void testCase(){
		Domain src = new Domain();
		//throws NPE
		Representation target = mapper.map(src, Representation.class);
		Assert.assertNotNull(target);
	}
}
