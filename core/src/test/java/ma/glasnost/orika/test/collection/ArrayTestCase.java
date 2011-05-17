package ma.glasnost.orika.test.collection;

import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class ArrayTestCase {

	@Test
	public void testMappingArrayOfString() {

		Product p = new Product();
		p.setTags(new String[] { "music", "sport" });

		ProductDTO productDTO = MappingUtil.getMapperFactory().getMapperFacade().map(p, ProductDTO.class);

		Assert.assertEquals(p.getTags(), productDTO.getTags());
	}

	public static class Product {

		private String[] tags;

		public String[] getTags() {
			return tags;
		}

		public void setTags(String[] tags) {
			this.tags = tags;
		}

	}

	public static class ProductDTO {

		private String[] tags;

		public String[] getTags() {
			return tags;
		}

		public void setTags(String[] tags) {
			this.tags = tags;
		}

	}
}
