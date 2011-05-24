package ma.glasnost.orika.impl.util;

final class CollectionUtil {

    private CollectionUtil() {

    }

	public static  boolean equalsAny(Object obj, Object... objects) {
		for (Object o : objects) {
			if (obj.equals(o)) {
				return true;
			}
		}
		return false;
	}

}
