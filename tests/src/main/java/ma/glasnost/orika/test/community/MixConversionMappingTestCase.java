/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package ma.glasnost.orika.test.community;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class MixConversionMappingTestCase {
    
    @Test
    public void testMixMapConvert() {
        
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(ClassMapBuilder.map(B.class, D.class).fieldMap("instanceSet").add().byDefault().toClassMap());
        
        factory.getConverterFactory().registerConverter(new CustomConverter<Set<A>, Set<C>>() {
            
            public Set<C> convert(Set<A> source, Type<? extends Set<C>> destinationType) {
                
                C c = new C();
                c.message = source.iterator().next().message + "-converted";
                
                Set<C> result = new HashSet<C>();
                result.add(c);
                
                return result;
            }
        });
        
        MapperFacade mapperFacade = factory.getMapperFacade();
        
        B b = new B();
        A a = new A();
        
        b.instanceSet = new HashSet<A>();
        b.instanceSet.add(a);
        a.message = "a";
        
        // Converter
        D d = mapperFacade.map(b, D.class);
        
        Assert.assertEquals("a-converted", d.instanceSet.iterator().next().message);
        
        // Default mapping
        d.instanceSet.iterator().next().message = "c";
        b = mapperFacade.map(d, B.class);
        
        Assert.assertEquals("c", b.instanceSet.iterator().next().message);
    }
    
    public static class A {
        
        public String message;
    }
    
    public static class B {
        
        public Set<A> instanceSet = new HashSet<MixConversionMappingTestCase.A>();
    }
    
    public static class C {
        
        public String message;
    }
    
    public static class D {
        
        public Set<C> instanceSet;
    }
}
