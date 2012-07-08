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
package ma.glasnost.orika.test.community;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.UtilityResolver;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.metadata.TypeFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 */
public class CustomMergerTest {

	
	@Test
	public void testMergingWithCustomMapper() {
		
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.registerMapper(new MergingMapper());
		factory.registerConcreteType(new TypeBuilder<Collection<Entity>>(){}.build(), 
				new TypeBuilder<ArrayList<Entity>>(){}.build());
		MapperFacade mapper = factory.getMapperFacade();
		
		List<Dto> dtos = new ArrayList<Dto>();
		
		Dto dto = new Dto();
		dto.setId(1L);
		dto.setName("A");
		dtos.add(dto);
		
		Dto dto2 = new Dto();
		dto2.setId(2L);
		dto2.setName("B");
		dtos.add(dto2);
		
		dto = new Dto();
		dto.setId(3L);
		dto.setName("C");
		dtos.add(dto);
		
		DtoHolder source = new DtoHolder();
		source.setEntities(dtos);
		
		Type<?> typeOf_DtoHolder = TypeFactory.valueOf(DtoHolder.class);
		UtilityResolver.getDefaultPropertyResolverStrategy().getProperties(typeOf_DtoHolder);
		
		final EntityHolder entities = mapper.map(source, EntityHolder.class);
		
		
		Assert.assertNotNull(entities);
		Assert.assertEquals(3, entities.getEntities().size());
		
		final EntityHolder originalEntities = entities;
		source.getEntities().remove(dto2);
		dto2.setName("B-Changed");
		source.getEntities().add(dto2);
		
		mapper.map(source, entities);
		
		Assert.assertEquals(entities.getEntities().size(), originalEntities.getEntities().size());
		
		Iterator<Entity> entitiesIter = entities.getEntities().iterator();
		Iterator<Entity> originalIter = originalEntities.getEntities().iterator();
		while (entitiesIter.hasNext()) {
		    Entity e = entitiesIter.next();
		    Entity o = originalIter.next();
		    Assert.assertSame(e, o);
		}
	}
	
	
	
	public static class MergingMapper extends CustomMapper<Collection<Dto>, Collection<Entity>> {

		public void mapAtoB(Collection<Dto> a, Collection<Entity> b,
				MappingContext context) {
			merge(a, b);
		}

		private Collection<Entity> merge(Collection<Dto> srcDtos, Collection<Entity> dstEntities) {

			Set<Long> ids = new HashSet<Long>(srcDtos.size());
			for (Dto memberDto : srcDtos) {
				Entity memberEntity = findEntity(dstEntities, memberDto.getId());
				if (memberEntity == null) {
					dstEntities.add(mapperFacade.map(memberDto, Entity.class));
				} else {
					mapperFacade.map(memberEntity, memberDto);
				}
				ids.add(memberDto.getId());
			}

			for (Iterator<Entity> iterator = dstEntities.iterator(); iterator.hasNext();) {
				Entity dstEntity = iterator.next();
				if (!dstEntity.isNew() && !ids.contains(dstEntity.getId())) {
					iterator.remove();
				}
			}

			return dstEntities;

		}

		private Entity findEntity(Collection<Entity> dstEntities, Long id) {
			for (Entity dstEntity : dstEntities) {
				if (id.equals(dstEntity.getId())) {
					return dstEntity;
				}
			}
			return null;
		}

	}

	
	
	
	public static class EntityHolder {
		
		private Collection<Entity> entityList;

		public Collection<Entity> getEntities() {
			return entityList;
		}

		public void setEntities(Collection<Entity> entityList) {
			this.entityList = entityList;
		}
		
	}
	
	public static class DtoHolder {
		
		private Collection<Dto> dtoList;

		public Collection<Dto> getEntities() {
			return dtoList;
		}

		public void setEntities(Collection<Dto> dtoList) {
			this.dtoList = dtoList;
		}
		
	}
	
	public static class Entity {

		private Long id;
		private String name;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isNew() {
			return id == null;
		}
	}

	public static class Dto {
		private Long id;
		private String name;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
