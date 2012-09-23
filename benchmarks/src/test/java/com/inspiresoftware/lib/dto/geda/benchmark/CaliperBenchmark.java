/*
 * This code is distributed under The GNU Lesser General Public License (LGPLv3)
 * Please visit GNU site for LGPLv3 http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright Denis Pavlov 2009
 * Web: http://www.inspire-software.com
 * SVN: https://geda-genericdto.svn.sourceforge.net/svnroot/geda-genericdto
 */

package com.inspiresoftware.lib.dto.geda.benchmark;

import java.util.HashSet;
import java.util.Set;

import com.google.caliper.Param;
import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Address;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Country;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Graph;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Name;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Person;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Point;
import com.inspiresoftware.lib.dto.geda.benchmark.domain.Segment;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.AddressDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.dto.PersonDTO;
import com.inspiresoftware.lib.dto.geda.benchmark.support.dozer.DozerBasicMapper;
import com.inspiresoftware.lib.dto.geda.benchmark.support.geda.GeDABasicMapper;
import com.inspiresoftware.lib.dto.geda.benchmark.support.manual.ManualBasicMapper;
import com.inspiresoftware.lib.dto.geda.benchmark.support.modelmapper.ModelMapperMapper;
import com.inspiresoftware.lib.dto.geda.benchmark.support.orika.OrikaMapper;
import com.inspiresoftware.lib.dto.geda.benchmark.support.orika.OrikaNonCyclicMapper;

/**
 * Caliper powered benchmark.
 * <p/>
 * User: denispavlov
 * Date: Sep 17, 2012
 * Time: 8:35:29 AM
 */
public class CaliperBenchmark extends SimpleBenchmark {


    public enum Library {

        //JAVA_MANUAL(new ManualBasicMapper()),
        
        ORIKA(new OrikaMapper()),
//        ORIKA_NOCYCLES(new OrikaNonCyclicMapper()),
        GEDA(new GeDABasicMapper()),
//        MODELMAPPER(new ModelMapperMapper()),
//        DOZER(new DozerBasicMapper());
;
        
        private Mapper mapper;

        Library(final Mapper mapper) {
            this.mapper = mapper;
        }
    }

    @Param
    private Library lib;
    
    @Param({ /*"1",*/ "100"/*, "10000"/*, "25000" */})
    private int length;

    private Person personLoaded;
    private PersonDTO personDTOLoaded;
    private Graph graphLoaded;

    private Mapper mapper;

    @Override
    protected void setUp() throws Exception {

        final Name name = new Name("Sherlock", "Holmes");
        final Country country = new Country("United Kingdom");
        final Address address = new Address("221B Baker Street", null, "London", country, "NW1 6XE");
        final Person entity = new Person(name, address);

        personLoaded = entity;

        final PersonDTO dto = new PersonDTO();
        dto.setFirstName("Sherlock");
        dto.setLastName("Holmes");
        final AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressLine1("221B Baker Street");
        addressDTO.setCity("London");
        addressDTO.setPostCode("NW1 6XE");
        addressDTO.setCountryName("United Kingdom");
        dto.setCurrentAddress(addressDTO);

        personDTOLoaded = dto;

        
        final Graph graph = new Graph();
        Set<Segment> segments = new HashSet<Segment>();
        Set<Point> points = new HashSet<Point>();
        Point point1 = null;
        Point point2 = null;
        for (int i=0; i < 20; ++i) {
            for (int j=0; j < 20; ++j) {
                for (int k=0; k < 20; ++k) {
                    Point point = new Point();
                    point.setX(i);
                    point.setY(j);
                    point.setZ(k);
                    if (k % 2 == 0) {
                        point1 = point;
                    } else {
                        point2 = point;
                    }
                    points.add(point);
                    if (point1 != null && point2 != null) {
                        Segment segment = new Segment();
                        segment.setPoint1(point1);
                        segment.setPoint2(point2);
                        segments.add(segment);
                    }
                }
            }
        }
        graph.setPoints(points);
        graph.setSegments(segments);
        
        graphLoaded = graph;
        
        
        mapper = lib.mapper;
    }

    public void timeFromDTOToEntity(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int ii = 0; ii < length; ii++) {
                mapper.fromDto(personDTOLoaded);
            }
        }
    } 
    
    public void timeFromEntityToDTO(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int ii = 0; ii < length; ii++) {
                mapper.fromEntity(personLoaded);
            }
        }
    }
    
    public void timeNestedEntityToDTO(int reps) {
        for (int i = 0; i < reps; i++) {
            for (int ii = 0; ii < length; ii++) {
                mapper.fromEntity(personLoaded);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String mode = System.getenv("mode");
        if ("profile1".equals(mode)) {
            
            CaliperBenchmark bm = new CaliperBenchmark();
            bm.length = 1000;
            bm.lib = Library.ORIKA;
            bm.setUp();
            
            System.out.println("Press any key when ready");
            System.in.read();
            
            //Runner.main(CaliperBenchmark.class, args);
            bm.timeFromEntityToDTO(10000);
            
            System.out.println("Finished; Press any key when ready");
            System.in.read();
        } else if ("profile2".equals(mode)) {
            CaliperBenchmark bm = new CaliperBenchmark();
            bm.length = 1000;
            bm.lib = Library.ORIKA;
            bm.setUp();
            
            System.out.println("Press any key when ready");
            System.in.read();
            
            //Runner.main(CaliperBenchmark.class, args);
            bm.timeFromDTOToEntity(10000);
            
            System.out.println("Finished; Press any key when ready");
            System.in.read();
        } else {
            Runner.main(CaliperBenchmark.class, args);
        }
    }

}
