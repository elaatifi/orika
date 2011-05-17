package ma.glasnost.orika.impl.util;

import java.math.BigDecimal;
import java.util.Date;

public final class ClassUtil {

	public static final String CGLIB_ID = "$$EnhancerByCGLIB$$";
	public static final String JAVASSIST_PACKAGE = "org.javassist.tmp.";
	public static final String JAVASSIST_NAME = "_$$_javassist_";

	private ClassUtil() {

	}

	public static final boolean isImmutable(Class<?> clazz) {
		return CollectionUtil.equalsAny(clazz, String.class, Integer.class, Long.class, Boolean.class, Character.class,
				Byte.class, Double.class, Float.class, BigDecimal.class, Integer.TYPE, Boolean.TYPE, Long.TYPE, Float.TYPE,
				Double.TYPE, Character.TYPE, Date.class, java.sql.Date.class)
				|| clazz.isEnum();
	}

	public static final boolean isProxy(Class<?> clazz) {
		if (clazz.isInterface()) {
			return false;
		}
		String className = clazz.getName();
		return className.contains(CGLIB_ID) || className.startsWith(JAVASSIST_PACKAGE) || className.contains(JAVASSIST_NAME);
	}
}
