/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark.support.manual;

import java.util.HashSet;

import com.inspiresoftware.lib.dto.geda.benchmark.Mapper;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Address;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Country;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Graph;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Name;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Person;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Segment;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.AddressDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.GraphDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.PersonDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.PointDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.SegmentDTO;

/**
 * .
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 10:29:18 AM
 */
public class ManualBasicMapper implements Mapper {

    public Object fromEntity(final Object entity) {

        final Person person = (Person) entity;
        final PersonDTO dto = new PersonDTO();
        if (person.getName() != null) {
            dto.setFirstName(person.getName().getFirstname());
            dto.setLastName(person.getName().getSurname());
        }
        if (person.getCurrentAddress() != null) {
            final Address address = person.getCurrentAddress();
            final AddressDTO addressDTO = new AddressDTO();
            addressDTO.setAddressLine1(address.getAddressLine1());
            addressDTO.setAddressLine2(address.getAddressLine2());
            addressDTO.setCity(address.getCity());
            addressDTO.setPostCode(address.getPostCode());
            if (address.getCountry() != null) {
                addressDTO.setCountryName(address.getCountry().getName());
            }
            dto.setCurrentAddress(addressDTO);
        }
        return dto;
    }

    public Object fromDto(final Object dto) {

        final Person person = new Person();
        final PersonDTO personDTO = (PersonDTO) dto;

        person.setName(new Name(personDTO.getFirstName(), personDTO.getLastName()));

        if (personDTO.getCurrentAddress() != null) {
            final Address address = new Address();
            final AddressDTO addressDTO = personDTO.getCurrentAddress();

            address.setAddressLine1(addressDTO.getAddressLine1());
            address.setAddressLine2(addressDTO.getAddressLine2());
            address.setCity(addressDTO.getCity());
            address.setPostCode(addressDTO.getPostCode());
            address.setCountry(new Country(addressDTO.getCountryName()));

            person.setCurrentAddress(address);
        }
        return person;
    }

    
    public Object fromEntityNested(Object entity) {
        GraphDTO graphDto = new GraphDTO();
        graphDto.setPoints(new HashSet<PointDTO>());
        graphDto.setSegments(new HashSet<SegmentDTO>());
        Graph source = (Graph)entity;
        for (Segment segment: source.getSegments()) {
            SegmentDTO s = new SegmentDTO();
            PointDTO p1 = new PointDTO();
            p1.setX(segment.getPoint1().getX());
            p1.setY(segment.getPoint1().getY());
            p1.setZ(segment.getPoint1().getZ());
            PointDTO p2 = new PointDTO();
            p2.setX(segment.getPoint2().getX());
            p2.setY(segment.getPoint2().getY());
            p2.setZ(segment.getPoint2().getZ());
            s.setPoint1(p1);
            s.setPoint2(p2);
            
            graphDto.getSegments().add(s);
            graphDto.getPoints().add(p1);
            graphDto.getPoints().add(p2);
        }
        return graphDto;
     }
}
