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
package ma.glasnost.orika.test.extensibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.Assert;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.metadata.FieldMap;
import ma.glasnost.orika.metadata.Property;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.property.PropertyResolverStrategy;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.junit.Test;

/**
 * This test case demonstrates how one could extend ClassMapBuilder to define
 * your own method for matching fields up between types.<br>
 * This example uses a scoring mechanism to rank how "close" two fields are to
 * each other based on their names, and starts by mapping the closest matches
 * first.
 * 
 * 
 * @author matt.deboer@gmail.com
 * 
 */
public class ClassMapBuilderExtensibilityTestCase {

	/**
	 * FieldMatchScore is used to rank "closeness" of a given field mapping
	 * 
	 * @author matt.deboer@gmail.com
	 * 
	 */
	private static class FieldMatchScore implements Comparable<FieldMatchScore> {

		private boolean contains;
		private boolean containsIgnoreCase;
		private boolean typeMatch;
		private int distance;
		private int distanceIgnoreCase;
		private Property propertyA;
		private Property propertyB;

		public FieldMatchScore(Property propertyA, Property propertyB) {
			String propertyALower = propertyA.getName().toLowerCase();
			String propertyBLower = propertyB.getName().toLowerCase();

			this.contains = propertyA.getName().contains(propertyB.getName())
					|| propertyB.getName().contains(propertyA.getName());
			this.containsIgnoreCase = contains
					|| propertyALower.contains(propertyBLower)
					|| propertyBLower.contains(propertyALower);
			this.distance = StringUtils.getLevenshteinDistance(
					propertyA.getName(), propertyB.getName());
			this.distanceIgnoreCase = StringUtils.getLevenshteinDistance(
					propertyALower, propertyBLower);
			this.propertyA = propertyA;
			this.propertyB = propertyB;
			this.typeMatch = propertyA.getType().isAssignableFrom(
					propertyB.getType())
					|| propertyB.getType()
							.isAssignableFrom(propertyA.getType());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(FieldMatchScore that) {
			if (this.containsIgnoreCase && !that.containsIgnoreCase) {
				return -1;
			} else if (!this.containsIgnoreCase && that.containsIgnoreCase) {
				return 1;
			}

			if (this.contains && !that.contains) {
				return -1;
			} else if (!this.contains && that.contains) {
				return 1;
			}

			if (this.distanceIgnoreCase < that.distanceIgnoreCase) {
				return -1;
			} else if (this.distanceIgnoreCase > that.distanceIgnoreCase) {
				return 1;
			}

			if (this.distance < that.distance) {
				return -1;
			} else if (this.distance > that.distance) {
				return 1;
			}

			if (this.typeMatch && !that.typeMatch) {
				return -1;
			} else if (!this.typeMatch && that.typeMatch) {
				return 1;
			}

			if (this.propertyA.getName().length() > that.propertyA.getName()
					.length()) {
				return -1;
			} else if (this.propertyA.getName().length() > that.propertyA
					.getName().length()) {
				return 1;
			}

			int propACompare = this.propertyA.getName().compareTo(
					that.propertyA.getName());
			if (propACompare < 0) {
				return -1;
			} else if (propACompare > 0) {
				return 1;
			}

			return this.propertyB.getName().compareTo(that.propertyB.getName());
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

	}

	public static class ScoringClassMapBuilder<A, B> extends
			ClassMapBuilder<A, B> {

		public static class Factory extends ClassMapBuilderFactory {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder
			 * (ma.glasnost.orika.metadata.Type,
			 * ma.glasnost.orika.metadata.Type,
			 * ma.glasnost.orika.property.PropertyResolverStrategy,
			 * ma.glasnost.orika.DefaultFieldMapper[])
			 */
			@Override
			protected <A, B> ClassMapBuilder<A, B> newClassMapBuilder(
					Type<A> aType, Type<B> bType,
					PropertyResolverStrategy propertyResolver,
					DefaultFieldMapper[] defaults) {

				return new ScoringClassMapBuilder<A, B>(aType, bType,
						propertyResolver, defaults);
			}

		}

		/**
		 * @param aType
		 * @param bType
		 * @param propertyResolver
		 * @param defaults
		 */
		protected ScoringClassMapBuilder(Type<A> aType, Type<B> bType,
				PropertyResolverStrategy propertyResolver,
				DefaultFieldMapper[] defaults) {
			super(aType, bType, propertyResolver, defaults);
		}

		public Map<String, Property> getPropertyExpressions(Type<?> type) {

			PropertyResolverStrategy propertyResolver = getPropertyRessolver();

			Map<String, Property> properties = new HashMap<String, Property>();
			LinkedHashMap<String, Property> toProcess = new LinkedHashMap<String, Property>(
					propertyResolver.getProperties(type));

			while (!toProcess.isEmpty()) {

				Entry<String, Property> entry = toProcess.entrySet().iterator().next();
				if (!entry.getKey().equals("class")) {

					if (!ClassUtil.isImmutable(entry.getValue().getType())) {
						Map<String, Property> props = propertyResolver
								.getProperties(entry.getValue().getType());
						if (!props.isEmpty()) {
							for (Entry<String, Property> property : props
									.entrySet()) {
								if (!property.getKey().equals("class")) {
									String expression = entry.getKey() + "." + property.getKey();
									toProcess.put(expression, resolveProperty(type, expression));
								}
							}
						} else {
							properties.put(
									entry.getKey(), resolveProperty(type, entry.getKey()));
						}
					} else {
						properties.put(
								entry.getKey(),	resolveProperty(type, entry.getKey()));
					}
				}
				toProcess.remove(entry.getKey());
			}
			return properties;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * ma.glasnost.orika.metadata.ClassMapBuilder#byDefault(ma.glasnost.
		 * orika.DefaultFieldMapper[])
		 */
		public ClassMapBuilder<A, B> byDefault(
				DefaultFieldMapper... withDefaults) {

			DefaultFieldMapper[] defaults;
			if (withDefaults.length == 0) {
				defaults = getDefaultFieldMappers();
			} else {
				defaults = withDefaults;
			}
			/*
			 * For our custom 'byDefault' method, we're going to try and match
			 * fields by their Levenshtein distance
			 */
			TreeSet<FieldMatchScore> matchScores = new TreeSet<FieldMatchScore>();

			Map<String, Property> propertiesForA = getPropertyExpressions(getAType());
			Map<String, Property> propertiesForB = getPropertyExpressions(getBType());

			for (final Entry<String, Property> propertyA : propertiesForA.entrySet()) {
				if (!propertyA.getValue().getName().equals("class")) {
					for (final Entry<String, Property> propertyB : propertiesForB
							.entrySet()) {
						if (!propertyB.getValue().getName().equals("class")) {
							matchScores.add(new FieldMatchScore(propertyA
									.getValue(), propertyB.getValue()));
						}
					}
				}
			}

			Set<String> unmatchedFields = new HashSet<String>(
					this.getPropertiesForTypeA());
			unmatchedFields.remove("class");

			for (FieldMatchScore score : matchScores) {

				if (!this.getMappedPropertiesForTypeA().contains(
						score.propertyA.getExpression())
						&& !this.getMappedPropertiesForTypeB().contains(
								score.propertyB.getExpression())) {

					fieldMap(score.propertyA.getExpression(),
							score.propertyB.getExpression()).add();
					unmatchedFields.remove(score.propertyA);
				}
			}

			/*
			 * Apply any default field mappers to the unmapped fields
			 */
			for (String propertyNameA : unmatchedFields) {
				Property prop = resolvePropertyForA(propertyNameA);
				for (DefaultFieldMapper defaulter : defaults) {
					String suggestion = defaulter.suggestMappedField(
							propertyNameA, prop.getType());
					if (suggestion != null
							&& getPropertiesForTypeB().contains(suggestion)) {
						if (!getMappedPropertiesForTypeB().contains(suggestion)) {
							fieldMap(propertyNameA, suggestion).add();
						}
					}
				}
			}

			return this;
		}

	}

	public static class Name {
		public String first;
		public String middle;
		public String last;
	}
	
	public static class Source {
		public String lastName;
		public Integer age;
		public String postalAddress;
		public String firstName;
		public String stateOfBirth;
	}

	public static class Destination {
		public Name name;
		public Integer currentAge;
		public String address;
		public String birthState;
	}

	@Test
	public void testClassMapBuilderExtension() {

		MapperFactory factory = new DefaultMapperFactory.Builder()
				.classMapBuilderFactory(new ScoringClassMapBuilder.Factory())
				.build();

		ClassMap<Source, Destination> map = factory.classMap(Source.class, Destination.class).byDefault().toClassMap();
		Map<String, String> mapping = new HashMap<String, String>();
		for (FieldMap f: map.getFieldsMapping()) {
			mapping.put(f.getSource().getExpression(), f.getDestination().getExpression());
		}
		
		Assert.assertEquals("name.first", mapping.get("firstName"));
		Assert.assertEquals("name.last", mapping.get("lastName"));
		Assert.assertEquals("address", mapping.get("postalAddress"));
		Assert.assertEquals("currentAge", mapping.get("age"));
		Assert.assertEquals("birthState", mapping.get("stateOfBirth"));
		
	}

}
