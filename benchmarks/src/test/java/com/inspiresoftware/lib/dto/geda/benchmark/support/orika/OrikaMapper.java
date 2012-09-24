/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.support.orika;

import ma.glasnost.orika.DedicatedMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;

import com.inspiresoftware.lib.dto.geda.benchmark.Mapper;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Address;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Graph;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Person;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.AddressDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.GraphDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.PersonDTO;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 12:04:50 PM
 */
public class OrikaMapper implements Mapper {

    private DedicatedMapperFacade<Person, PersonDTO> mapper;
    private DedicatedMapperFacade<Graph, GraphDTO> graphMapper;
    
    public OrikaMapper() {
        final MapperFactory factory = new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy())
                .build();
        factory.registerClassMap(factory.classMap(Address.class, AddressDTO.class).
            field("addressLine1", "addressLine1").
            field("addressLine2", "addressLine2").
            field("postCode", "postCode").
            field("city", "city").
            field("country.name", "countryName").
            toClassMap()
        );
        factory.registerClassMap(factory.classMap(Person.class, PersonDTO.class).
            field("name.firstname", "firstName").
            field("name.surname", "lastName").
            field("currentAddress", "currentAddress").
            toClassMap()
        );
        factory.registerClassMap(factory.classMap(Graph.class, GraphDTO.class));
        
        this.mapper = factory.dedicatedMapperFor(Person.class, PersonDTO.class, true);
        this.graphMapper = factory.dedicatedMapperFor(Graph.class, GraphDTO.class);
    }

    public Object fromEntity(final Object entity) {
        PersonDTO dto = new PersonDTO();
        mapper.mapAtoB((Person) entity, dto);
        return dto;
    }

    public Object fromDto(final Object dto) {
        Person entity = new Person();
        mapper.mapBtoA((PersonDTO) dto, entity);
        return entity;
    }

    public Object fromEntityNested(Object entity) {
        GraphDTO dto = new GraphDTO();
        graphMapper.mapAtoB((Graph) entity, dto);
        return dto;
    }
}
