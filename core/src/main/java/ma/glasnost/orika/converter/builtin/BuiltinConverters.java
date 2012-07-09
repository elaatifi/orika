package ma.glasnost.orika.converter.builtin;

import java.io.File;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import ma.glasnost.orika.converter.ConverterFactory;

/**
 * BuiltinConverters is a utility class used to register common built-in converters.
 * 
 * @author mattdeboer
 *
 */
public abstract class BuiltinConverters {
    
    /**
     * Registers a common set of built-in converters which can handle many common conversion situations.<br>
     * Specifically, this includes:
     * <ul>
     * <li>ConstructorConverter: converts from the source type to destination type if there is
     * a constructor available on the destination type which takes the source as a single argument.
     * <li>FromStringConverter: able to convert from a String to enum, primitive, or primitive wrapper.
     * <li>ToStringconverter: able to convert any type to String
     * <li>DateAndTimeConverters: convert between common data/time representations<ul>
     * <li>CalendarToXmlGregorianCalendarConverter
     * <li>DateToCalendarConverter
     * <li>DateToXmlGregorianCalendarConverter
     * <li>LongToCalendarConverter
     * <li>LongToDateConverter
     * <li>LongToXmlGregorianCalendarConverter
     * </ul>
     * <li>PassThroughConverter registered for the following (additional) immutable types:<ul>
     * <li>java.net.URL
     * <li>java.net.URI
     * <li>java.util.UUID
     * <li>java.math.BigInteger
     * <li>java.util.Locale
     * <li>java.io.File
     * <li>java.net.Inet4Address
     * <li>java.net.Inet6Address
     * <li>java.net.InetSocketAddress
     * </ul>
     * </ul>
     * 
     * @param converterFactory the converter factory on which to register the converters
     */
    public static void register(ConverterFactory converterFactory) {
        
        /*
         * Register converter to instantiate by using a constructor on
         * the destination which takes the source as argument
         */
        converterFactory.registerConverter(new ConstructorConverter());
        
        /*
         * Register to/from string converters
         */
        converterFactory.registerConverter(new FromStringConverter());
        converterFactory.registerConverter(new ToStringConverter());
        
        /*
         * Register common date/time converters
         */
        converterFactory.registerConverter(new DateAndTimeConverters.CalendarToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.DateToXmlGregorianCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToCalendarConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToDateConverter());
        converterFactory.registerConverter(new DateAndTimeConverters.LongToXmlGregorianCalendarConverter());
        
        /*
         * Register additional common "immutable" types
         */
        converterFactory.registerConverter(new PassThroughConverter(
              URL.class,
              URI.class,
              UUID.class,
              BigInteger.class,
              Locale.class,
              File.class,
              Inet4Address.class,
              Inet6Address.class,
              InetSocketAddress.class
                ));
        
    }
}
