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

package ma.glasnost.orika.test.generics;

import java.io.Serializable;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class GenericsTestCase {

	@Test
	public void testTypeErasure() {
		MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
		EntityLong entity = new EntityLong();
		entity.setId(42L);

		new EntityGeneric<String>().setId("Hello");
		new EntityGeneric<Integer>().setId(42);
		EntityLong clone = mapperFacade.map(entity, EntityLong.class);

		Assert.assertEquals(42L, clone.getId());
	}

	@Test
	public void testTypeErasure2() {
		MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
		EntityLong entity = new EntityLong();
		entity.setId(42L);

		new EntityGeneric<String>().setId("Hello");
		EntityGeneric<Long> sourceObject = new EntityGeneric<Long>();
		sourceObject.setId(42L);
		EntityLong clone = mapperFacade.map(sourceObject, EntityLong.class);

		Assert.assertEquals(42L, clone.getId());
	}

	public static interface Entity<T extends Serializable> {
		public T getId();

		public void setId(T id);
	}

	public static class EntityLong implements Entity<Long> {
		private Long id;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

	}

	public static class EntityString implements Entity<String> {
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

	public static class EntityGeneric<T extends Serializable> implements Entity<T> {
		private T id;

		public T getId() {
			return id;
		}

		public void setId(T id) {
			this.id = id;
		}

	}

}
