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
package ma.glasnost.orika.test.benchmarks.strategy;

import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.test.benchmarks.domain.destination.AddressDto;
import ma.glasnost.orika.test.benchmarks.domain.destination.PersonDto;
import ma.glasnost.orika.test.benchmarks.domain.source.Address;
import ma.glasnost.orika.test.benchmarks.domain.source.Country;
import ma.glasnost.orika.test.benchmarks.domain.source.Name;
import ma.glasnost.orika.test.benchmarks.domain.source.Person;

import com.inspiresoftware.lib.dto.geda.adapter.BeanFactory;
import com.inspiresoftware.lib.dto.geda.assembler.Assembler;
import com.inspiresoftware.lib.dto.geda.assembler.DTOAssembler;


/**
 *
 */
public abstract class GeDaMapper<S, D> implements MappingProvider<S, D> {

    
    private static final class GeDABeanFactory implements BeanFactory {

        /** {@inheritDoc} */
        public Object get(final String entityBeanKey) {
            if ("addressDto".equals(entityBeanKey)) {
                return new AddressDto();
            } else if ("countryEntity".equals(entityBeanKey)) {
                return new Country();
            } else if ("nameEntity".equals(entityBeanKey)) {
                return new Name();
            } else if ("addressEntity".equals(entityBeanKey)) {
                return new Address();
            }
            return null;
        }
    }
    
    private Type<S> sourceClass;
    private Type<D> destClass;
    
    private BeanFactory bf;
    private Assembler asm; 

    
    public void initialize() { 
        bf = new GeDABeanFactory();
        asm = DTOAssembler.newAssembler(PersonDto.class, Person.class);
    }
    
    /* (non-Javadoc)
     * @see ma.glasnost.orika.test.benchmarks.strategy.MappingProvider#mapTo(java.lang.Object)
     */
    @Override
    public D mapTo(S source, Class<D> destClass) {
        D dest = (D) bf.get(destClass.getSimpleName());
        asm.assembleDto(source, dest, null, bf);
        return dest;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.test.benchmarks.strategy.MappingProvider#mapFrom(java.lang.Object)
     */
    @Override
    public S mapFrom(D destination, Class<S> sourceClass) {
        S source = (S) bf.get(sourceClass.getSimpleName());
        asm.assembleEntity(destination, source, null, bf);
        return source;
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.test.benchmarks.strategy.MappingProvider#mapTo(java.lang.Object, java.lang.Object)
     */
    @Override
    public void mapTo(S source, D destination) {
        asm.assembleDto(source, destination, null, bf);
    }

    /* (non-Javadoc)
     * @see ma.glasnost.orika.test.benchmarks.strategy.MappingProvider#mapFrom(java.lang.Object, java.lang.Object)
     */
    @Override
    public void mapFrom(D destination, S source) {
        asm.assembleEntity(destination, source, null, bf);
    }
}
