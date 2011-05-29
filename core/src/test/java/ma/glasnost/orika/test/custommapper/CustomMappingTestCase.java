package ma.glasnost.orika.test.custommapper;

import junit.framework.Assert;
import ma.glasnost.orika.MapperBase;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class CustomMappingTestCase {

	@Test
	public void testCustomMapping() {
		MapperFactory factory = MappingUtil.getMapperFactory();

		factory.registerClassMap(ClassMapBuilder.map(PersonDTO.class, Person.class).customize(
				new MapperBase<PersonDTO, Person>() {
					@Override
					public void mapBtoA(Person b, PersonDTO a, MappingContext context) {
						a.setName(b.getFirstName() + " " + b.getLastName());
					}

				}).toClassMap());

		Person person = new Person();
		person.setFirstName("Abdelkrim");
		person.setLastName("EL KHETTABI");

		PersonDTO dto = factory.getMapperFacade().map(person, PersonDTO.class);

		Assert.assertEquals(dto.getName(), person.getFirstName() + " " + person.getLastName());
	}

	public static class PersonDTO {
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

	public static class Person {
		private String firstName;
		private String lastName;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
}
