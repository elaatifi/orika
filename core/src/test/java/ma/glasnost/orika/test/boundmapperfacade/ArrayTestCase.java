package ma.glasnost.orika.test.boundmapperfacade;

import java.util.Arrays;

import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.array.ArrayTestCaseClasses;
import ma.glasnost.orika.test.array.ArrayTestCaseClasses.A;
import ma.glasnost.orika.test.array.ArrayTestCaseClasses.B;
import ma.glasnost.orika.test.array.ArrayTestCaseClasses.C;
import ma.glasnost.orika.test.array.ArrayTestCaseClasses.D;

import org.junit.Assert;
import org.junit.Test;

public class ArrayTestCase {

    @Test
    public void testSimplePrimitiveArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        BoundMapperFacade<A,B> mapperFacade = MappingUtil.getMapperFactory().getBoundMapperFacade(ArrayTestCaseClasses.A.class, ArrayTestCaseClasses.B.class);

        ArrayTestCaseClasses.B destination = mapperFacade.mapAtoB(source);

        Assert.assertArrayEquals(source.getBuffer(), destination.getBuffer());
        
    }

    @Test
    public void testSimplePrimitiveToWrapperArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        BoundMapperFacade<A,C> mapperFacade = MappingUtil.getMapperFactory().getBoundMapperFacade(A.class, C.class);

        ArrayTestCaseClasses.C destination = mapperFacade.mapAtoB(source);

        Assert.assertArrayEquals(new Byte[]{1,2,3,4}, destination.getBuffer());
    }
    
    @Test
    public void testArrayToList() {
        BoundMapperFacade<A, D> mapperFacade = MappingUtil.getMapperFactory().getBoundMapperFacade(A.class, D.class);
    	
    	ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);


        D destination = mapperFacade.mapAtoB(source);

        Assert.assertEquals(Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4), destination.getBuffer());
    	
    }
    
    @Test
    public void testListToArray() {
        BoundMapperFacade<A,D> mapperFacade = MappingUtil.getMapperFactory().getBoundMapperFacade(A.class, D.class);
    	
    	D source =  new D();
        source.setBuffer(Arrays.asList((byte)1,(byte)2,(byte)3,(byte)4));


        A destination = mapperFacade.mapBtoA(source);

        Assert.assertArrayEquals(new byte[] {(byte)1,(byte)2,(byte)3,(byte)4}, destination.getBuffer());
    	
    }
    
    @Test
    public void testMappingArrayOfString() {

        Product p = new Product();
        p.setTags(new String[] { "music", "sport" });

        ProductDTO productDTO = MappingUtil.getMapperFactory().getBoundMapperFacade(Product.class, ProductDTO.class).mapAtoB(p);

        Assert.assertArrayEquals(p.getTags(), productDTO.getTags());
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
