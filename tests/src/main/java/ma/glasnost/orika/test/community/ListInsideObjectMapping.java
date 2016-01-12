package ma.glasnost.orika.test.community;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class Issue119TestCase {
    
    public static class Source {
        public String id;
        public SourceRef ref;
    }
    
    public static class SourceRef {
        public List<String> identities;
        public List<Complex> comp1;
    }
    
    public static class Dest {
        public String id;
        public DestReference references;
        //public List<DestReference> references;
    }
    
    public static class DestReference {
        //public String identity;
    	 public List<String> identity;
    	 public List<ComplexRef> comp2;
    }
    
    public static class Complex {
    	
    	public int a;
    	public int b;
    }
    
    public static class ComplexRef {
    	
    	public int aRef;
    	public int bRef;
    }
    
    @Test
    public void test() {
        
        MapperFactory factory = MappingUtil.getMapperFactory(true);
        
        factory.classMap(Complex.class, ComplexRef.class)
        .fieldAToB("a", "aRef")
        .fieldAToB("b","bRef")
        .register();
        
        factory.classMap(Source.class, Dest.class)
                .mapNulls(false)
                .fieldAToB("id", "id")
                .fieldAToB("ref", "references")
                .fieldAToB("ref.identities{}", "references.identity{}")
                .fieldAToB("ref.comp1{}", "references.comp2{}")
                .register();
        
        Source src = new Source();
        src.id = "myId";
        SourceRef srcRef = new SourceRef();
        srcRef.identities = Arrays.asList("hello", "world");
        src.ref = srcRef;
        Complex c1 = new Complex();
        c1.a = -100;
        c1.b = -200;
        srcRef.comp1 = new ArrayList<Issue119TestCase.Complex>();
        srcRef.comp1.add(c1);
        Complex c2 = new Complex();
        c2.a = -109;
        c2.b = -209;
        srcRef.comp1.add(c2);
        Complex c3 = new Complex();
        c3.a = 1000000;
        c3.b = 5000000;
        srcRef.comp1.add(c3);
        
        Dest dest = factory.getMapperFacade().map(src, Dest.class);
        System.out.println(dest);
        
    }
}
