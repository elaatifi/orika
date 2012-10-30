package ma.glasnost.orika.test.custommapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMap;
import ma.glasnost.orika.metadata.ScoringClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class ExpanderTestCase {
    
    public static class Year {
        public int yearNumber;
        public String yearAnimal;
        public List<Month> months = new ArrayList<Month>();
    }
    
    public static class Month {
        public int monthNumber;
        public String monthName;
        public List<Day> days = new ArrayList<Day>();
    }
    
    public static class Day {
        public int dayNumber;
        public String dayOfWeek;
    }
    
    public static class FlatData {
        public int dayNumber;
        public String dayOfWeek;
        public int yearNumber;
        public String yearAnimal;
        public int monthNumber;
        public String monthName;
    }
    
    @Test
    public void testExpand() {
        
        Type<List<FlatData>> typeOf_FlatData = new TypeBuilder<List<FlatData>>(){}.build();
        Type<List<Year>> typeOf_Year = new TypeBuilder<List<Year>>(){}.build();
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder()
            .classMapBuilderFactory(new ScoringClassMapBuilder.Factory())
            .compilerStrategy(new EclipseJdtCompilerStrategy())
            .build();
        
        ClassMap<List<FlatData>, List<Year>> classMap = 
                mapperFactory.classMap(typeOf_FlatData, typeOf_Year)
                    .byDefault().toClassMap();
        Assert.assertNotNull(classMap);
        
        mapperFactory.registerClassMap(classMap);
        
        MapperFacade mapper = mapperFactory.getMapperFacade();
        
        
        List<FlatData> flatData = new ArrayList<FlatData>();
        FlatData item = new FlatData();
        item.dayNumber = 1;
        item.dayOfWeek = "Monday";
        item.monthNumber = 10;
        item.monthName = "October";
        item.yearNumber = 2011;
        item.yearAnimal = "monkey";
        
        flatData.add(item);
        item = new FlatData();
        item.dayNumber = 2;
        item.dayOfWeek = "Tuesday";
        item.monthNumber = 12;
        item.monthName = "December";
        item.yearNumber = 2011;
        item.yearAnimal = "monkey";
        flatData.add(item);
        
        List<Year> years = mapper.map(flatData, typeOf_FlatData, typeOf_Year);
        
        Assert.assertNotNull(years);
        Assert.assertFalse(years.isEmpty());
        Assert.assertEquals(1, years.size());
        Year year = years.get(0);
        Assert.assertEquals(2011, year.yearNumber);
        Assert.assertEquals(2, year.months.size());
    }
    
    /**
     * Takes a list of flattened data elements and expands them into a hierarchical
     * object graph who's root elements are of the target type provided.
     * 
     * @param from
     * @param toType
     * @return
     */
    private static void expand(List<FlatData> source, List<Year> destination) {
        
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        // goal is to expand a list of flattened data (such as you might receive
        // from an SQL result set)
        // into a hierarchical object graph, by auto-detecting where the nested
        // values should be placed
        
        // 1. need to detect and match up all of the nested properties
        // (including element properties)
        // from the destination tpe
        Iterator<FlatData> sourceIter = source.iterator();
        List<Year> newDestination = new ArrayList<Year>();
        
        Year currentDestinationElement = null; // newObjectMap
        Month currentMonth = null;
        Day currentDay = null;
        
        while (sourceIter.hasNext()) {
            FlatData sourceElement = sourceIter.next();
            
            if (currentDestinationElement == null || 
                    // comparator for currentDestinationElement
                    (currentDestinationElement.yearNumber != sourceElement.yearNumber 
                        || !currentDestinationElement.yearAnimal.equals(sourceElement.yearAnimal))
                    ) {
                // Add currentDestinationElement to it's container
                newDestination.add(currentDestinationElement);
                currentDestinationElement = new Year();
                
            }
            mapperFacade.map(sourceElement, currentDestinationElement);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
            if (currentMonth == null || 
                    // comparator for currentMonth
                    (currentMonth.monthNumber != sourceElement.monthNumber)
                    ) {
                // Add currentMonth to it's container
                if (currentMonth != null) {
                    currentDestinationElement.months.add(currentMonth);
                }
                currentMonth = new Month();
                
            }
            mapperFacade.map(sourceElement, currentMonth);
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
            if (currentDay == null || 
                    // comparator for currentMonth
                    (currentDay.dayNumber != sourceElement.dayNumber)
                    ) {
                // Add currentMonth to it's container
                currentMonth.days.add(currentDay);
                currentDay = new Day();
                
            }
            mapperFacade.map(sourceElement, currentDay);

        }
        
        
        // Add currentDestinationElement to it's container
        newDestination.add(currentDestinationElement);
        // Add currentMonth to it's container
        currentDestinationElement.months.add(currentMonth);
        // Add currentMonth to it's container
        currentMonth.days.add(currentDay);
        
        
        // Add new destination to the target destination
        destination.clear();
        destination.addAll(newDestination);
    }
    
    private static void contract( List<Year> source, List<FlatData> destination) {
        
        MapperFacade mapperFacade = MappingUtil.getMapperFactory().getMapperFacade();
        // goal is to expand a list of flattened data (such as you might receive
        // from an SQL result set)
        // into a hierarchical object graph, by auto-detecting where the nested
        // values should be placed
        
        // 1. need to detect and match up all of the nested properties
        // (including element properties)
        // from the destination tpe
        Iterator<Year> sourceIter = source.iterator();
        List<FlatData> newDestination = new ArrayList<FlatData>();
        
        FlatData currentDestinationElement = null; // newObjectMap
        while (sourceIter.hasNext()) {
            Year sourceElement = sourceIter.next();
            
            mapperFacade.map(sourceElement, currentDestinationElement);
            
            Iterator<Month> month_sourceIter = sourceElement.months.iterator();
            while (month_sourceIter.hasNext()) {
                
                Month months_sourceElement = month_sourceIter.next();
                mapperFacade.map(months_sourceElement, currentDestinationElement);
                
                Iterator<Day> days_sourceIter = months_sourceElement.days.iterator();
                while( days_sourceIter.hasNext()) {
                    
                    Day days_sourceElement = days_sourceIter.next();
                    if (currentDestinationElement == null || 
                            // comparator for currentDestinationElement
                            (currentDestinationElement.yearNumber != sourceElement.yearNumber)
                            ) {
                        currentDestinationElement = new FlatData();
                        // Add currentDestinationElement to it's container
                        newDestination.add(currentDestinationElement);
                    }
                    
                    mapperFacade.map(days_sourceElement, currentDestinationElement);
                    
                }
                
                
                
            }
        }        
    
    }
}
