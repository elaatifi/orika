package ma.glasnost.orika.converter.builtin;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

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
public class DateAndTimeConverters {

	
	/**
	 * Provides conversion between Date and Calendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class DateToCalendarConverter extends
			BidirectionalConverter<Date, Calendar> {

		@Override
		public Calendar convertTo(Date source, Type<Calendar> destinationType) {
			return toCalendar(source);
		}

		@Override
		public Date convertFrom(Calendar source, Type<Date> destinationType) {
			return toDate(source);
		}
	}

	/**
	 * Provides conversion between Date and XMLGregorianCalendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class DateToXmlGregorianCalendarConverter extends
			BidirectionalConverter<Date, XMLGregorianCalendar> {

		@Override
		public XMLGregorianCalendar convertTo(Date source,
				Type<XMLGregorianCalendar> destinationType) {
			return toXMLGregorianCalendar(source);
		}

		@Override
		public Date convertFrom(XMLGregorianCalendar source,
				Type<Date> destinationType) {
			return toDate(source);
		}
	}

	/**
	 * Provides conversion between Calendar and XMLGregorianCalendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class CalendarToXmlGregorianCalendarConverter extends
			BidirectionalConverter<Calendar, XMLGregorianCalendar> {

		@Override
		public XMLGregorianCalendar convertTo(Calendar source,
				Type<XMLGregorianCalendar> destinationType) {
			return toXMLGregorianCalendar(source);
		}

		@Override
		public Calendar convertFrom(XMLGregorianCalendar source,
				Type<Calendar> destinationType) {
			return toCalendar(source);
		}
	}

	/**
	 * Provides conversion between Long and XMLGregorianCalendar
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class LongToXmlGregorianCalendarConverter extends
			BidirectionalConverter<Long, XMLGregorianCalendar> {

		@Override
		public XMLGregorianCalendar convertTo(Long source,
				Type<XMLGregorianCalendar> destinationType) {
			return toXMLGregorianCalendar(source);
		}

		@Override
		public Long convertFrom(XMLGregorianCalendar source,
				Type<Long> destinationType) {
			return toLong(source);
		}
	}

	/**
	 * Provides conversion between Long and Date
	 * 
	 * @author matt.deboer@gmail.com
	 *
	 */
	public static class LongToDateConverter extends
			BidirectionalConverter<Long, Date> {

		@Override
		public Date convertTo(Long source, Type<Date> destinationType) {
			return toDate(source);
		}

		@Override
		public Long convertFrom(Date source, Type<Long> destinationType) {
			return toLong(source);
		}
	}

	/**
	 * Provides conversion between Long and Calendar
	 * 
	 * @author matt.deboer@gmail.com
	 *
	 */
	public static class LongToCalendarConverter extends
			BidirectionalConverter<Long, Calendar> {

		@Override
		public Calendar convertTo(Long source, Type<Calendar> destinationType) {
			return toCalendar(source);
		}

		@Override
		public Long convertFrom(Calendar source, Type<Long> destinationType) {
			return toLong(source);
		}
	}

	private static Date toDate(XMLGregorianCalendar source) {
		return source.toGregorianCalendar().getTime();
	}

	private static Date toDate(Calendar source) {
		return source.getTime();
	}

	private static Date toDate(Long source) {
		return new Date(source);
	}

	private static Calendar toCalendar(XMLGregorianCalendar source) {
		return toCalendar(source.toGregorianCalendar().getTime());
	}

	private static Calendar toCalendar(Date source) {
		Calendar c = Calendar.getInstance();
		c.setTime(source);
		return c;
	}

	private static Calendar toCalendar(Long source) {
		return toCalendar(new Date(source));
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Calendar source) {
		return toXMLGregorianCalendar(source.getTime());
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Date source) {
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(source);
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		} catch (DatatypeConfigurationException e) {
			throw new IllegalStateException(e);
		}
	}

	private static XMLGregorianCalendar toXMLGregorianCalendar(
			Long source) {
		return toXMLGregorianCalendar(new Date(source));
	}

	private static Long toLong(Date source) {
		return source.getTime();
	}

	private static Long toLong(Calendar source) {
		return toLong(source.getTime());
	}

	private static Long toLong(XMLGregorianCalendar source) {
		return toLong(source.toGregorianCalendar().getTime());
	}
}
