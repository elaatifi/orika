package ma.glasnost.orika.impl.util;

import java.math.BigDecimal;
import java.util.Date;

public abstract class ClassUtil {

	public static final boolean isImmutable(Class<?> clazz) {
		return CollectionUtil.equalsAny(clazz, String.class, Integer.class, Long.class, Boolean.class, Character.class,
				Byte.class, Double.class, Float.class, BigDecimal.class, Integer.TYPE, Boolean.TYPE, Long.TYPE, Float.TYPE,
				Double.TYPE, Character.TYPE, Date.class, java.sql.Date.class)
				|| clazz.isEnum();
	}
}
