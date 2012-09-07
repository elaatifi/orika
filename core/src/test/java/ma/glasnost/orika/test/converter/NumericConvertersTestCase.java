package ma.glasnost.orika.test.converter;

import java.math.BigDecimal;
import java.math.BigInteger;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingException;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigDecimalToDoubleConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigDecimalToFloatConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigIntegerToIntegerConverter;
import ma.glasnost.orika.converter.builtin.NumericConverters.BigIntegerToLongConverter;
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
public class NumericConvertersTestCase {

	
	@Test
	public void testBigDecimalToDoubleConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigDecimalToDoubleConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		BigDecimal bd = new BigDecimal("5423.51478");
		Double db = mapper.map(bd, Double.class);
		Assert.assertEquals(bd.doubleValue(), db.doubleValue(), 0.00001d);
		
		BigDecimal reverse = mapper.map(db, BigDecimal.class);
		Assert.assertEquals(bd.doubleValue(), reverse.doubleValue(), 0.00001d);
	}
	
	@Test
	public void testBigDecimalToFloatConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigDecimalToFloatConverter());
		MapperFacade mapper = factory.getMapperFacade();
		
		BigDecimal bd = new BigDecimal("5423.51");
		Float ft = mapper.map(bd, Float.class);
		Assert.assertEquals(bd.floatValue(), ft.floatValue(), 0.01d);
		
		BigDecimal reverse = mapper.map(ft, BigDecimal.class);
		Assert.assertEquals(bd.doubleValue(), reverse.doubleValue(), 0.01d);
	}
	
	@Test
	public void testBigIntegerToLongConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigIntegerToLongConverter(false));
		MapperFacade mapper = factory.getMapperFacade();
		
		BigInteger bi = new BigInteger(""+Long.MAX_VALUE);
		Long lg = mapper.map(bi, Long.class);
		Assert.assertEquals(bi.longValue(), lg.longValue());
		
		BigInteger reverse = mapper.map(lg, BigInteger.class);
		Assert.assertEquals(bi.longValue(), reverse.longValue());
	}
	
	@Test
	public void testBigIntegerToIntegerConverter() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigIntegerToIntegerConverter(false));
		MapperFacade mapper = factory.getMapperFacade();
		
		BigInteger bi = new BigInteger(""+Integer.MAX_VALUE);
		Integer i = mapper.map(bi, Integer.class);
		Assert.assertEquals(bi.longValue(), i.longValue());
		
		BigInteger reverse = mapper.map(i, BigInteger.class);
		Assert.assertEquals(bi.longValue(), reverse.longValue());
	}
	
	
	@Test(expected=MappingException.class)
	public void testBigIntegerToLongConverter_Overflow() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigIntegerToLongConverter(false));
		MapperFacade mapper = factory.getMapperFacade();
		
		BigInteger bi = new BigInteger("1"+Long.MAX_VALUE);
		Long lg = mapper.map(bi, Long.class);
		Assert.assertEquals(bi.longValue(), lg.longValue());
		
		BigInteger reverse = mapper.map(lg, BigInteger.class);
		Assert.assertEquals(bi.longValue(), reverse.longValue());
	}
	
	@Test(expected=MappingException.class)
	public void testBigIntegerToIntegerConverter_Overflow() {
		MapperFactory factory = MappingUtil.getMapperFactory();
		factory.getConverterFactory().registerConverter(new BigIntegerToIntegerConverter(false));
		MapperFacade mapper = factory.getMapperFacade();
		
		BigInteger bi = new BigInteger("1"+Long.MAX_VALUE);
		Integer i = mapper.map(bi, Integer.class);
		Assert.assertEquals(bi.longValue(), i.longValue());
		
		BigInteger reverse = mapper.map(i, BigInteger.class);
		Assert.assertEquals(bi.longValue(), reverse.longValue());
	}
	
}
