package ma.glasnost.orika.proxy;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;

public class HibernateUnenhanceStrategy implements UnenhanceStrategy {

	@SuppressWarnings("unchecked")
	public <T> T unenhanceObject(T object) {
		if (object instanceof HibernateProxy) {
			HibernateProxy hibernateProxy = (HibernateProxy) object;
			LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();

			return (T) lazyInitializer.getImplementation();
		}
		return object;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> unenhanceClass(T object) {
		return HibernateProxyHelper.getClassWithoutInitializingProxy(object);
	}
}
