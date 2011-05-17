package ma.glasnost.orika.impl.util;

public abstract class CollectionUtil {

	public static final boolean equalsAny(Object obj, Object... objects) {
		for (Object o : objects) {
			if (obj.equals(o)) {
				return true;
			}
		}
		return false;
	}

}
