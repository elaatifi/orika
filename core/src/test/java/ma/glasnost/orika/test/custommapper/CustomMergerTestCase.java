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
package ma.glasnost.orika.test.custommapper;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ScoringClassMapBuilder;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class CustomMergerTestCase {
    
    
    public static class Year {
        public int yearNumber;
        public List<Month> months = new ArrayList<Month>();
    }
    
    public static class Month {
        public int monthNumber;
        public List<Day> days = new ArrayList<Day>();
    }
    
    public static class Day {
        public int dayNumber;
        public String dayOfWeek;
    }
    
    public static class FlatYearMonthDay {
        public int dayNumber;
        public String dayOfWeek;
        public int monthNumber;
        public int yearNumber;
    }
    
    
    /*
     * Special use case: merge unequal structures
     */
    @Test
    @Ignore
    public void testFlattenedListToStructure() {
        List<FlatYearMonthDay> flattenedData = new ArrayList<FlatYearMonthDay>();
        FlatYearMonthDay flat = new FlatYearMonthDay();
        flat.dayNumber = 1;
        flat.dayOfWeek = "Monday";
        flat.monthNumber = 2;
        flat.yearNumber = 2012;
        flattenedData.add(flat);
        flat = new FlatYearMonthDay();
        flat.dayNumber = 2;
        flat.dayOfWeek = "Wednesday";
        flat.monthNumber = 5;
        flat.yearNumber = 2012;
        flattenedData.add(flat);
        flattenedData.add(flat);
        flat = new FlatYearMonthDay();
        flat.dayNumber = 12;
        flat.dayOfWeek = "Friday";
        flat.monthNumber = 7;
        flat.yearNumber = 2012;
        flattenedData.add(flat);
        flattenedData.add(flat);
        flat = new FlatYearMonthDay();
        flat.dayNumber = 8;
        flat.dayOfWeek = "Tuesday";
        flat.monthNumber = 9;
        flat.yearNumber = 2012;
        flattenedData.add(flat);
        
        MapperFacade mapper = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new ScoringClassMapBuilder.Factory())
            .build().getMapperFacade();
        
        Year year = mapper.map(flattenedData, Year.class);
        
        Assert.assertNotNull(year);
        
    }
    
    
    private <F, T> T expand(List<F> flatData, Class<T> targetType) {
        
        // 1. get the properties for targetType, and look for any multi-occurrence properties
        
        // 2. 
        
        return null;
    }
    
}
