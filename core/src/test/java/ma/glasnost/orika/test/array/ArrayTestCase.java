package ma.glasnost.orika.test.array;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.MappingUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

public class ArrayTestCase {

    @Test
    public void testSimplePrimitiveArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();

        ArrayTestCaseClasses.B destination = mapperFacade.map(source, ArrayTestCaseClasses.B.class);

        Assert.assertArrayEquals(source.getBuffer(), destination.getBuffer());
        
    }

    @Test
    @Ignore
    public void testSimplePrimitiveToWrapperArray() {
        ArrayTestCaseClasses.A source =  new ArrayTestCaseClasses.A();
        byte[] buffer = new byte[]{1,2,3,4};
        source.setBuffer(buffer);

        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();

        ArrayTestCaseClasses.C destination = mapperFacade.map(source, ArrayTestCaseClasses.C.class);

        Assert.assertArrayEquals(new Byte[]{1,2,3,4}, destination.getBuffer());
    }
}
