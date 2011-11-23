package ma.glasnost.orika.test.constructor;

import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;
import ma.glasnost.orika.Converter;
import ma.glasnost.orika.ConverterException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.GeneratedSourceCode;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class ConstructorMappingTestCase {
    
    @Test
    public void testSimpleCase() throws Throwable {
        
    	System.setProperty(GeneratedSourceCode.PROPERTY_WRITE_SOURCE_FILES, "true");
    	
    	final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(ClassMapBuilder.map(PersonVO.class, Person.class)
                .constructorA()
                .field("dateOfBirth", "date")
                .byDefault()
                .toClassMap());
        factory.registerConverter(new Converter<Date, String>() {
            public String convert(Date source) throws ConverterException {
                return df.format(source);
            }
        }, Date.class, String.class);
        
        factory.build();
        
        Person person = new Person();
        person.setFirstName("Abdelkrim");
        person.setLastName("EL KHETTABI");
        person.setDate(df.parse("01/01/1980"));
        person.setAge(31L);
        
        PersonVO vo = factory.getMapperFacade().map(person, PersonVO.class);
        
        Assert.assertEquals(person.getFirstName(), vo.getFirstName());
        Assert.assertEquals(person.getLastName(), vo.getLastName());
        Assert.assertTrue(person.getAge() == vo.getAge());
        Assert.assertEquals("01/01/1980", vo.getDateOfBirth());
    }
    
    public static class Person {
        private String firstName;
        private String lastName;
        
        private Long age;
        private Date date;
        
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
        
        public Long getAge() {
            return age;
        }
        
        public void setAge(Long age) {
            this.age = age;
        }
        
        public Date getDate() {
            return date;
        }
        
        public void setDate(Date date) {
            this.date = date;
        }
        
    }
    
    public static class PersonVO {
        private final String firstName;
        private final String lastName;
        
        private final long age;
        private final String dateOfBirth;
        
        public PersonVO(String firstName, String lastName, long age, String dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
            this.dateOfBirth = dateOfBirth;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public long getAge() {
            return age;
        }
        
        public String getDateOfBirth() {
            return dateOfBirth;
        }
    }
}
