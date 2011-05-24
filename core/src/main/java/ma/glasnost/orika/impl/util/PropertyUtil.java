package ma.glasnost.orika.impl.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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

public final class PropertyUtil {

	private static final Map<Class<?>, Map<String, Property>> PROPERTIES_CACHE = new ConcurrentHashMap<Class<?>, Map<String, Property>>();

	private PropertyUtil() {

	}

	public static Map<String, Property> getProperties(Class<?> clazz) {
		Map<String, Property> properties = new HashMap<String, Property>();
		if (PROPERTIES_CACHE.containsKey(clazz)) {
			return PROPERTIES_CACHE.get(clazz);
		}
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
			for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
				try {
					Property property = new Property();
					property.setName(pd.getName());
					if (pd.getReadMethod() != null)
						property.setGetter(pd.getReadMethod().getName());
					if (pd.getWriteMethod() != null)
						property.setSetter(pd.getWriteMethod().getName());
					property.setType(pd.getReadMethod().getDeclaringClass().getDeclaredMethod(property.getGetter(), new Class[0])
							.getReturnType());
					properties.put(pd.getName(), property);

					if (pd.getReadMethod() != null) {
						Method method = pd.getReadMethod();
						if (property.getType() != null && Collection.class.isAssignableFrom(property.getType())) {
							property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericReturnType())
									.getActualTypeArguments()[0]);
						}
					} else if (pd.getWriteMethod() != null) {
						Method method = pd.getWriteMethod();

						if (Collection.class.isAssignableFrom(property.getType()) && method.getGenericParameterTypes().length > 0)
							property.setParameterizedType((Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0])
									.getActualTypeArguments()[0]);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		} catch (IntrospectionException e) {
			/* Ignore */
		}

		PROPERTIES_CACHE.put(clazz, Collections.unmodifiableMap(properties));
		return properties;
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
