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

package ma.glasnost.orika.test.primitives;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class BooleanTestCase {

	@Test
	public void testPrimtiveToWrapper() {
		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.registerClassMap(ClassMapBuilder.map(Pojo.class, Pojo.class).field("primitive", "wrapper").toClassMap());

		MapperFacade mapper = factory.getMapperFacade();

		Pojo source = new Pojo();
		source.setPrimitive(true);

		Pojo destination = mapper.map(source, Pojo.class);
		Assert.assertEquals(Boolean.TRUE, destination.getWrapper());

	}

	@Test
	public void testWrapperToPrimtive() {
		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.registerClassMap(ClassMapBuilder.map(Pojo.class, Pojo.class).field("wrapper", "primitive").toClassMap());

		MapperFacade mapper = factory.getMapperFacade();

		Pojo source = new Pojo();
		source.setWrapper(true);

		Pojo destination = mapper.map(source, Pojo.class);
		Assert.assertEquals(true, destination.isPrimitive());

	}

	public static class Pojo {
		private boolean primitive;
		private Boolean wrapper;

		public boolean isPrimitive() {
			return primitive;
		}

		public void setPrimitive(boolean primitive) {
			this.primitive = primitive;
		}

		public Boolean getWrapper() {
			return wrapper;
		}

		public void setWrapper(Boolean wrapper) {
			this.wrapper = wrapper;
		}
	}
}
