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
package ma.glasnost.orika.converter.builtin;

import java.math.BigDecimal;
import java.math.BigInteger;

import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;

/**
 * NumericConverters contains a set of common conversions between the "big" value
 * types in the java.math package (BigDecimal and BigInteger) and their related 
 * primitive wrapper types.
 * 
 * @author matt.deboer@gmail.com
 *
 */
public class NumericConverters {

	/**
	 * Provides conversion between BigDecimal and Double.<br><br>
	 * <strong>Note:</strong> please consider the typical warnings regarding 
	 * loss of precision when converting from BigDecimal to Double.
	 * 
	 * @see BigDecimal#doubleValue()
	 * @author matt.deboer@gmail.com
	 */
	public static class BigDecimalToDoubleConverter extends
			BidirectionalConverter<BigDecimal, Double> {

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertTo(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public Double convertTo(BigDecimal source, Type<Double> destinationType) {
			return source.doubleValue();
		}

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertFrom(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public BigDecimal convertFrom(Double source,
				Type<BigDecimal> destinationType) {
			return BigDecimal.valueOf(source.doubleValue());
		}
	}
	
	/**
	 * Provides conversion between BigDecimal and Float.<br><br>
	 * <strong>Note:</strong> please consider the typical warnings regarding 
	 * loss of precision when converting from BigDecimal to Float.
	 * 
	 * @see BigDecimal#floatValue()
	 * @author matt.deboer@gmail.com
	 */
	public static class BigDecimalToFloatConverter extends
			BidirectionalConverter<BigDecimal, Float> {

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertTo(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public Float convertTo(BigDecimal source, Type<Float> destinationType) {
			return source.floatValue();
		}

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertFrom(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public BigDecimal convertFrom(Float source,
				Type<BigDecimal> destinationType) {
			return BigDecimal.valueOf(source.doubleValue());
		}
	}
	
	/**
	 * Provides conversion between BigInteger and Long.
	 * 
	 * @see BigInteger
	 * @author matt.deboer@gmail.com
	 */
	public static class BigIntegerToLongConverter extends
			BidirectionalConverter<BigInteger, Long> {

		private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
		private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);
		private final boolean truncate;
		
		/**
		 * Constructs a new BigIntegerToLongConverter, with the configured truncation behavior.
		 * 
		 * @param truncate specifies whether the converter should perform truncation; if false,
		 * an ArithmeticException is thrown for a value which is too large or too small to be
		 * accurately represented by java.lang.Long
		 */
		public BigIntegerToLongConverter(boolean truncate) {
			this.truncate = truncate;
		}
		
		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertTo(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public Long convertTo(BigInteger source, Type<Long> destinationType) {
			if (!truncate && (source.compareTo(MAX_LONG) > 0 || source.compareTo(MIN_LONG) < 0)) {
				throw new ArithmeticException("Overflow: " + source + " cannot be represented by " + Long.class.getCanonicalName());
			}
			return source.longValue();
		}

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertFrom(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public BigInteger convertFrom(Long source,
				Type<BigInteger> destinationType) {
			return BigInteger.valueOf(source.longValue());
		}
	}
	
	/**
	 * Provides conversion between BigInteger and Integer
	 * 
	 * @author matt.deboer@gmail.com
	 */
	public static class BigIntegerToIntegerConverter extends
			BidirectionalConverter<BigInteger, Integer> {

		private static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
		private static final BigInteger MIN_INT = BigInteger.valueOf(Integer.MIN_VALUE);
		
		private final boolean truncate;
		
		/**
		 * Constructs a new BigIntegerToIntegerConverter, with the configured truncation behavior.
		 * 
		 * @param truncate specifies whether the converter should perform truncation; if false,
		 * an ArithmeticException is thrown for a value which is too large or too small to be
		 * accurately represented by java.lang.Integer
		 */
		public BigIntegerToIntegerConverter(boolean truncate) {
			this.truncate = truncate;
		}
		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertTo(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public Integer convertTo(BigInteger source, Type<Integer> destinationType) {
			if (!truncate && (source.compareTo(MAX_INT) > 0 || source.compareTo(MIN_INT) < 0)) {
				throw new ArithmeticException("Overflow: " + source + " cannot be represented by " + Integer.class.getCanonicalName());
			}
			return source.intValue();
		}

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.converter.BidirectionalConverter#convertFrom(java.lang.Object, ma.glasnost.orika.metadata.Type)
		 */
		@Override
		public BigInteger convertFrom(Integer source,
				Type<BigInteger> destinationType) {
			return BigInteger.valueOf(source.longValue());
		}
	}
	
}
