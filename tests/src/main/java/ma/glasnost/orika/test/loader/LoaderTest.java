package ma.glasnost.orika.test.loader;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.loader.XMLParser;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.ClassMapBuilderFactory;
import ma.glasnost.orika.metadata.ClassMapBuilderForMaps;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.property.PropertyResolverStrategy;
import ma.glasnost.orika.test.MappingUtil;
import ma.glasnost.orika.test.community.collection.AbstractMapperTest;
import ma.glasnost.orika.test.tuple.TupleElementProxy;
import ma.glasnost.orika.test.tuple.TupleProxy;
import ma.glasnost.orika.test.unenhance.AuthorDTO;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LoaderTest {
    @Test
    public void testTupleObject() {
        String fn = getClass().getClassLoader().getResource("config.xml").getFile();
        MapperFactory factory = MappingUtil.getMapperFactory();
        XMLParser parser = new XMLParser(factory);
        parser.reader(fn);

        A a = new A();
        a.setA1("11");
        a.setA2(12);
        a.setA3("14");

        B b = factory.getMapperFacade().map(a, B.class);

        System.out.println(b);
    }
}
