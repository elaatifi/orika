package ma.glasnost.orika.impl.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ma.glasnost.orika.metadata.NestedProperty;
import ma.glasnost.orika.metadata.Property;

public abstract class PropertyUtil {

	public static Map<Class<?>, Map<String, Property>> propertiesCache = new ConcurrentHashMap<Class<?>, Map<String, Property>>();

	public static Map<String, Property> getProperties(Class<?> clazz) {
		Map<String, Property> properties = new HashMap<String, Property>();
		ofClass(clazz, properties);

		propertiesCache.put(clazz, Collections.unmodifiableMap(properties));
		return properties;
	}

	// public static Set<String> intersectNames(Class<?> classA, Class<?>
	// classB) {
	// Map<String, Property> a = getProperties(classA);
	// Map<String, Property> b = getProperties(classB);
	//
	// Set<String> properties = new HashSet<String>();
	// for (String propertyName : a.keySet()) {
	// if (b.containsKey(propertyName)) {
	// properties.add(propertyName);
	// }
	// }
	//
	// return properties;
	// }

	private static void ofClass(Class<?> clazz, Map<String, Property> properties) {
		for (Method method : clazz.getDeclaredMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("get") || methodName.startsWith("is")) {
				final int pos = methodName.startsWith("is") ? 2 : 3;
				String propertyName = Character.toLowerCase(methodName.charAt(pos)) + methodName.substring(pos + 1);
				Property property = getOrPutProperty(properties, method, propertyName);
				property.setType(method.getReturnType());
				property.setGetter(methodName);

				if (Collection.class.isAssignableFrom(property.getType()))
					property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericReturnType())
							.getActualTypeArguments()[0]);
			} else if (methodName.startsWith("set") && method.getParameterTypes().length == 1) {
				final int pos = 3;
				String propertyName = Character.toLowerCase(methodName.charAt(pos)) + methodName.substring(pos + 1);
				Property property = getOrPutProperty(properties, method, propertyName);
				property.setSetter(methodName);
				property.setType(method.getParameterTypes()[0]);

				// destinationGeneric = method.getGenericParameterTypes()[0];
				if (Collection.class.isAssignableFrom(property.getType()) && method.getGenericParameterTypes().length > 0)
					property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0])
							.getActualTypeArguments()[0]);
			}
		}
	}

	private static Property getOrPutProperty(Map<String, Property> properties, Method method, String propertyName) {
		Property property = properties.get(propertyName);
		if (property == null) {
			property = new Property();
			property.setName(propertyName);
			properties.put(propertyName, property);
		}
		return property;
	}

	public static NestedProperty getNestedProperty(Class<?> clazz, String p) {
		Map<String, Property> properties = getProperties(clazz);
		Property property = null;
		List<Property> path = new ArrayList<Property>();
		if (p.indexOf('.') != -1) {
			String[] ps = p.split("\\.");
			int i = 0;
			while (i < ps.length) {
				if (!properties.containsKey(ps[i]))
					throw new RuntimeException(clazz.getName() + " do not contains [" + ps[i] + "] property.");
				property = properties.get(ps[i]);
				properties = getProperties(property.getType());
				i++;
				if (i < ps.length)
					path.add(property);
			}
		}

		if (property == null)
			throw new RuntimeException(clazz.getName() + " do not contains [" + p + "] property.");

		return new NestedProperty(property, path.toArray(new Property[path.size()]));
	}

	public static boolean isExpression(String a) {
		return a.indexOf('.') != -1;
	}
}
