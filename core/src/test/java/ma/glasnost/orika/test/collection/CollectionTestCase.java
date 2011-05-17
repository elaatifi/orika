package ma.glasnost.orika.test.collection;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class CollectionTestCase {

	@Test
	public void testStringToString() {
		D source = new D();
		source.setTags(Arrays.asList("soa", "java", "rest"));

		A destination = MappingUtil.getMapperFactory().getMapperFacade().map(source, A.class);

		Assert.assertNotNull(destination.getTags());
		Assert.assertEquals(3, destination.getTags().size());
	}

	static public class A {
		private Set<String> tags;

		public Set<String> getTags() {
			return tags;
		}

		public void setTags(Set<String> tags) {
			this.tags = tags;
		}
	}

	public static class D {
		private List<String> tags;

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}
	}
}
