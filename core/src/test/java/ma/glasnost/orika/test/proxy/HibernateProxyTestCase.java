package ma.glasnost.orika.test.proxy;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.test.HibernateUtil;
import ma.glasnost.orika.test.MappingUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;

public class HibernateProxyTestCase {
    
    @Test
    public void testMappigProxyObject() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        MapperFacade mapper = MappingUtil.getMapperFactory().getMapperFacade();
        Transaction t = session.beginTransaction();
        
        {
            Author author = new Author();
            author.setName("Khalil Gebran");
            session.save(author);
            
            Book book = new Book();
            book.setTitle("The Prophet");
            book.setAuthor(author);
            
            session.save(book);
        }
        {
            Author author = new Author();
            author.setName("Mohamed CHAOUKI");
            session.save(author);
            
            Book book = new Book();
            book.setTitle("Le pain nu");
            book.setAuthor(author);
            
            session.save(book);
            
        }
        session.flush();
        t.commit();
        session.clear();
        
        AuthorDTO author = mapper.map((Author) session.load(Author.class, 1L), AuthorDTO.class);
        
        Assert.assertEquals("Khalil Gebran", author.getName());
    }
}
