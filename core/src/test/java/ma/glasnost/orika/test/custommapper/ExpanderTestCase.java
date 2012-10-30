package ma.glasnost.orika.test.custommapper;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ScoringClassMapBuilder;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;

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
            .build();
        
        mapperFactory.classMap(typeOf_FlatData, typeOf_Year).byDefault().register();
        
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
}
