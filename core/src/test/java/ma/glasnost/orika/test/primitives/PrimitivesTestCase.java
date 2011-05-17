package ma.glasnost.orika.test.primitives;

import java.util.Collection;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Assert;
import org.junit.Test;

public class PrimitivesTestCase {

	@Test
	public void testSimpleMapping() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		MapperFacade mapper = factory.getMapperFacade();

		A source = new A();

		source.setAge(27);
		source.setName("PPPPP");
		source.setSex('H');
		source.setVip(true);

		B destination = mapper.map(source, B.class);

		Assert.assertEquals(source.getAge(), destination.getAge());
		Assert.assertEquals(source.getName(), destination.getName());
		Assert.assertEquals(source.getSex(), destination.getSex());
		Assert.assertEquals(source.getVip(), destination.getVip());

	}

	public static class A {
		private int age;
		private String name;
		private char sex;
		private boolean vip;
		private Collection<String> tags;

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public char getSex() {
			return sex;
		}

		public void setSex(char sex) {
			this.sex = sex;
		}

		public boolean getVip() {
			return vip;
		}

		public void setVip(boolean vip) {
			this.vip = vip;
		}

		public Collection<String> getTags() {
			return tags;
		}

		public void setTags(Collection<String> tags) {
			this.tags = tags;
		}

	}

	public static class B {
		private Integer age;
		private String name;
		private Character sex;
		private Boolean vip;
		private Collection<String> tags;

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public void setName(String nom) {
			this.name = nom;
		}

		public Character getSex() {
			return sex;
		}

		public void setSex(Character sex) {
			this.sex = sex;
		}

		public Boolean getVip() {
			return vip;
		}

		public void setVip(Boolean vip) {
			this.vip = vip;
		}

		public Collection<String> getTags() {
			return tags;
		}

		public void setTags(Collection<String> tags) {
			this.tags = tags;
		}

	}
}
