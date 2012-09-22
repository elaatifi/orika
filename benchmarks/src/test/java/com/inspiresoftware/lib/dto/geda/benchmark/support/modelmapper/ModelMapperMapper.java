/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.support.modelmapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import com.inspiresoftware.lib.dto.geda.benchmark.Mapper;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Graph;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Person;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.GraphDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.PersonDTO;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 11:39:32 AM
 */
public class ModelMapperMapper implements Mapper {

    private ModelMapper mapperFromDto;
    private ModelMapper mapperFromEntity;
    private ModelMapper mapperFromGraph;

    public ModelMapperMapper() {
        mapperFromDto = new ModelMapper();
        mapperFromDto.addMappings(new PropertyMap<PersonDTO, Person>() {
            @Override
            protected void configure() {
                map().getName().setFirstname(source.getFirstName());
                map().getName().setSurname(source.getLastName());
            }
        });
        mapperFromEntity = new ModelMapper();
        mapperFromEntity.addMappings(new PropertyMap<Person, PersonDTO>() {
            @Override
            protected void configure() {
                map().setFirstName(source.getName().getFirstname());
                map().setLastName(source.getName().getSurname());
            }
        });
        mapperFromGraph = new ModelMapper();
        mapperFromGraph.addMappings(new PropertyMap<Graph, GraphDTO>() {
            @Override
            protected void configure() {
               /* no special mappings here */
            }
        });
        
    }

    public Object fromEntity(final Object entity) {
        PersonDTO dto = new PersonDTO();
        mapperFromEntity.map(entity, dto);
        return dto;
    }

    public Object fromDto(final Object dto) {
        Person entity = new Person();
        mapperFromDto.map(dto, entity);
        return entity;
    }

    /* (non-Javadoc)
     * @see com.inspiresoftware.lib.dto.geda.benchmark.Mapper#fromEntityNested(java.lang.Object)
     */
    @Override
    public Object fromEntityNested(Object entity) {
        GraphDTO dto = new GraphDTO();
        mapperFromDto.map(dto, entity);
        return entity;
    }
}
