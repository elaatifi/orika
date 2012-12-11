package ma.glasnost.orika.test.converter;

import java.util.Calendar;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.CalendarToXmlGregorianCalendarConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.DateToCalendarConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.DateToXmlGregorianCalendarConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.LongToCalendarConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.LongToDateConverter;
import ma.glasnost.orika.converter.builtin.DateAndTimeConverters.LongToXmlGregorianCalendarConverter;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

/**
 * DateAndTimeConverters provides a set of individual converters
 * for conversion between the below listed enumeration of commonly used data/time
 * representations:
 * <ul>
 * <li>java.util.Date
 * <li>java.util.Calendar
 * <li>java.lang.Long or long
 * <li>javax.xml.datatype.XMLGregorianCalendar
 * </ul>
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class DateAndTimeConvertersTestCase {

	
	@Test
	public void testDateToCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new DateToCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		Calendar cal = mapper.map(now, Calendar.class);
		Assert.assertEquals(now, cal.getTime());
		
		Date reverse = mapper.map(cal, Date.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
	public void testDateToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new DateToXmlGregorianCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Date now = new Date();
		XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
		Assert.assertEquals(now, xml.toGregorianCalendar().getTime());
		
		Date reverse = mapper.map(xml, Date.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
	public void testCalendarToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new CalendarToXmlGregorianCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		Calendar cal = Calendar.getInstance();
		XMLGregorianCalendar xml = mapper.map(cal, XMLGregorianCalendar.class);
		Assert.assertEquals(cal.getTime(), xml.toGregorianCalendar().getTime());
		
		Calendar reverse = mapper.map(xml, Calendar.class);
		Assert.assertEquals(cal, reverse);
	}

	@Test
	public void testLongToXmlGregorianCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new LongToXmlGregorianCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		XMLGregorianCalendar xml = mapper.map(now, XMLGregorianCalendar.class);
		
		Assert.assertEquals(now, xml.toGregorianCalendar().getTimeInMillis());
		
		long reverse = mapper.map(xml, Long.class);
		Assert.assertEquals(now, reverse);
	}
	
	@Test
	public void testLongToDateConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new LongToDateConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		Date date = mapper.map(now, Date.class);
		Assert.assertEquals(now, date.getTime());
		
		long reverse = mapper.map(date, Long.class);
		Assert.assertEquals(now, reverse);
	}

	@Test
	public void testLongToCalendarConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new LongToCalendarConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		long now = System.currentTimeMillis();
		Calendar cal = mapper.map(now, Calendar.class);
		Assert.assertEquals(now, cal.getTimeInMillis());
		
		long reverse = mapper.map(cal, Long.class);
		Assert.assertEquals(now, reverse);
	}

	
}
