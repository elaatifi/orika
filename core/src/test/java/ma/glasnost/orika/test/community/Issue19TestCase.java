package ma.glasnost.orika.test.community;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

import org.junit.Test;

public class Issue19TestCase {
	
	@Test
	public void test() {
		MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

		A a = new A();
		a.setAttribute("attribute");
		
		B b = new B();
		mapperFactory.getMapperFacade().map(a, b);
		Assert.assertEquals(a.getAttribute(),b.getAttribute());
		
		B b1 = new B();
		mapperFactory.getMapperFacade().map(a, b1);
		Assert.assertEquals(a.getAttribute(),b1.getAttribute());
	}
	
	static public class A {
		private String attribute;
		
		public String getAttribute() {
			return attribute;
		}
		
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
	}
	
	static public class B {
		private String attribute;
		
		public String getAttribute() {
			return attribute;
		}
		
		public void setAttribute(String attribute) {
			this.attribute = attribute;
		}
	}
}
