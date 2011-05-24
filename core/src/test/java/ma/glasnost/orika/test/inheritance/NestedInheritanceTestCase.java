package ma.glasnost.orika.test.inheritance;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.test.MappingUtil;

import org.junit.Test;

public class NestedInheritanceTestCase {
    
    @Test
    public void testNestedInheritance() {
        MapperFactory factory = MappingUtil.getMapperFactory();
        
        factory.registerClassMap(ClassMapBuilder.map(Person.class, PersonDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(Client.class, ClientDTO.class).byDefault().toClassMap());
        factory.registerClassMap(ClassMapBuilder.map(Subscription.class, SubscriptionDTO.class).field("client", "person").toClassMap());
        
        Client client = new Client();
        client.setName("Khalil Gebran");
        
        Subscription subscription = new Subscription();
        subscription.setClient(client);
        
        SubscriptionDTO dto = factory.getMapperFacade().map(subscription, SubscriptionDTO.class);
        
        Assert.assertNotNull(dto);
        Assert.assertNotNull(dto.getPerson());
        Assert.assertEquals(client.getName(), dto.getPerson().getName());
    }
    
    public abstract static class Person {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class Client extends Person {
        
    }
    
    public static class Subscription {
        
        private Person client;
        
        public Person getClient() {
            return client;
        }
        
        public void setClient(Person client) {
            this.client = client;
        }
        
    }
    
    public static abstract class PersonDTO {
        
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
    }
    
    public static class ClientDTO extends PersonDTO {
        
    }
    
    public static class SubscriptionDTO {
        private PersonDTO person;
        
        public PersonDTO getPerson() {
            return person;
        }
        
        public void setPerson(PersonDTO person) {
            this.person = person;
        }
        
    }
}
