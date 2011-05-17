package ma.glasnost.orika.impl;

import ma.glasnost.orika.impl.util.ClassUtil;
import ma.glasnost.orika.metadata.FieldMap;

public abstract class Specifications {

	public interface Specification {
		boolean apply(FieldMap fieldMap);
	}

	public static Specification immutable() {
		return IS_IMMUTABLE;
	}

	@Deprecated
	public static Specification compatibleTypes() {
		return HAVE_COMPATIBLE_TYPES;
	}

	public static Specification anArray() {
		return IS_ARRAY;
	}

	public static Specification aCollection() {
		return IS_COLLECTION;
	}

	public static Specification aPrimitive() {
		return IS_PRIMITIVE;
	}

	public static Specification aPrimitiveToWrapper() {
		return PRIMITIVE_TO_WRAPPER;
	}

	public static Specification aWrapperToPrimitive() {
		return WRAPPER_TO_PRIMITIVE;
	}

	static final Specification IS_IMMUTABLE = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return ClassUtil.isImmutable(fieldMap.getSource().getType())
					&& fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
		}
	};

	static final Specification HAVE_COMPATIBLE_TYPES = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return fieldMap.getDestination().isAssignableFrom(fieldMap.getSource());
		}
	};

	static final Specification IS_ARRAY = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			if (fieldMap.getDestination().isArray() && (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection())) {
				return true;
			}
			return false;
		}
	};

	static final Specification IS_COLLECTION = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return (fieldMap.getSource().isArray() || fieldMap.getSource().isCollection())
					&& fieldMap.getDestination().isCollection();
		}
	};
	static final Specification IS_PRIMITIVE = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return fieldMap.getSource().getType().isPrimitive() || fieldMap.getDestination().getType().isPrimitive();
		}
	};

	static final Specification WRAPPER_TO_PRIMITIVE = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return fieldMap.getDestination().isPrimitive() && !fieldMap.getSource().isPrimitive();
		}

	};

	static final Specification PRIMITIVE_TO_WRAPPER = new Specification() {

		public boolean apply(FieldMap fieldMap) {
			return !fieldMap.getDestination().isPrimitive() && fieldMap.getSource().isPrimitive();
		}

	};
}
