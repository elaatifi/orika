package ma.glasnost.orika.test;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

public final class HibernateUtil {
    
    private HibernateUtil() {
        
    }
    
    public static final SessionFactory sessionFactory;
    
    static {
        try {
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static final ThreadLocal session = new ThreadLocal();
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
