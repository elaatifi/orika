/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
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

package ma.glasnost.orika.test.inheritance;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class UsedMappersTestCase {
    
    @Test
    public void testReuseOfMapper() {
        MapperFactory mapperFactory = MappingUtil.getMapperFactory();
        {
            ClassMapBuilder<A, C> classMapBuilder = ClassMapBuilder.map(A.class, C.class);
            classMapBuilder.field("name", "nom");
            mapperFactory.registerClassMap(classMapBuilder.toClassMap());
        }
        
        {
            ClassMapBuilder<B, D> classMapBuilder = ClassMapBuilder.map(B.class, D.class);
            classMapBuilder.field("ages", "age").use(A.class, C.class);
            mapperFactory.registerClassMap(classMapBuilder.toClassMap());
        }
        
        mapperFactory.build();
        
        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        
        B source = new B();
        source.setName("Israfil");
        source.setAges(1000);
        
        D target = mapperFacade.map(source, D.class);
        
        Assert.assertEquals(source.getName(), target.getNom());
        Assert.assertEquals(source.getAges(), target.getAge());
        
    }
    
    public static abstract class A {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class B extends A {
        private int ages;
        
        public int getAges() {
            return ages;
        }
        
        public void setAges(int ages) {
            this.ages = ages;
        }
        
    }
    
    public static abstract class C {
        private String nom;
        
        public String getNom() {
            return nom;
        }
        
        public void setNom(String nom) {
            this.nom = nom;
        }
        
    }
    
    public static class D extends C {
        private int age;
        
        public int getAge() {
            return age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
        
    }
}
