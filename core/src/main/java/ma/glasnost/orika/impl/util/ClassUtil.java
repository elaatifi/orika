package ma.glasnost.orika.impl.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public final class ClassUtil {

	private static final String CGLIB_ID = "$$EnhancerByCGLIB$$";
	private static final String JAVASSIST_PACKAGE = "org.javassist.tmp.";
	private static final String JAVASSIST_NAME = "_$$_javassist_";
	private static final Set<Class<?>> IMMUTABLES_TYPES = getImmutablesTypes();

	private ClassUtil() {

	}

	@SuppressWarnings("unchecked")
	private static Set<Class<?>> getImmutablesTypes() {
		return new HashSet<Class<?>>(Arrays.asList(String.class, Integer.class, Long.class, Boolean.class, Character.class,
				Byte.class, Double.class, Float.class, BigDecimal.class, Integer.TYPE, Boolean.TYPE, Long.TYPE, Float.TYPE,
				Double.TYPE, Character.TYPE, Date.class, java.sql.Date.class));

	}

	public static boolean isImmutable(Class<?> clazz) {
		return IMMUTABLES_TYPES.contains(clazz) || clazz.isEnum();
	}

	public static boolean isProxy(Class<?> clazz) {
		if (clazz.isInterface()) {
			return false;
		}
		String className = clazz.getName();
		return className.contains(CGLIB_ID) || className.startsWith(JAVASSIST_PACKAGE) || className.contains(JAVASSIST_NAME);
	}
}
