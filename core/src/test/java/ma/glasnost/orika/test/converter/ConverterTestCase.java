package ma.glasnost.orika.test.converter;

import junit.framework.Assert;
import ma.glasnost.orika.ConverterBase;
import ma.glasnost.orika.ConverterException;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class ConverterTestCase {
    
    @Test
    public void testConvertLongString() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerConverter(new ConverterBase<Long, String>() {
            
            public String convert(Long source) throws ConverterException {
                return source.toString();
            }
        });
        
        factory.registerClassMap(ClassMapBuilder.map(A.class, B.class).field("id", "string").toClassMap());
        
        A source = new A();
        source.setId(42L);
        
        B destination = factory.getMapperFacade().map(source, B.class);
        
        Assert.assertEquals("42", destination.getString());
        
    }
    
    public static class A {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
    }
    
    public static class B {
        private String string;
        
        public String getString() {
            return string;
        }
        
        public void setString(String string) {
            this.string = string;
        }
        
    }
}
