package ma.glasnost.orika.impl.util;

abstract class StringUtil {

	public static String toString(String s) {
		return s;
	}

	public static String toString(float f) {
		return Float.toString(f);
	}

	public static String toString(long l) {
		return Long.toString(l);
	}

	public static String toString(byte b) {
		return Byte.toString(b);
	}

	public static String toString(char c) {
		return Character.toString(c);
	}

	public static String toString(double d) {
		return Double.toString(d);
	}

	public static String toString(short s) {
		return Short.toString(s);
	}

	public static String toString(Object object) {
		return object.toString();
	}
}
