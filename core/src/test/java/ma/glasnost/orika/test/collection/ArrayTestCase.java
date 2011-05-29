/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
