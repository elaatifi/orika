package ma.glasnost.orika.test.community.issue42;

import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.generator.EclipseJdtCompilerStrategy;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import org.junit.Test;

public class InheritanceTestcase {
    
    @Test
    public void testInheritance() {
        
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().compilerStrategy(new EclipseJdtCompilerStrategy()).build();
        
        mapperFactory.registerClassMap(ClassMapBuilder.map(BaseEntity.class, BaseEntityDto.class).byDefault().toClassMap());
        
        ClassMapBuilder<BaseUser, BaseUserDto> userClassMapBuilder = ClassMapBuilder.map(BaseUser.class, BaseUserDto.class);
        userClassMapBuilder.customize(new CustomMapper<BaseUser, BaseUserDto>() {
            @Override
            public void mapAtoB(BaseUser a, BaseUserDto b, MappingContext mappingContext) {
                b.setName(a.getName() + " [mapped from BaseUser to BaseUserDto]");
            }
            
            @Override
            public void mapBtoA(BaseUserDto b, BaseUser a, MappingContext mappingContext) {
                a.setName(b.getName() + " [mapped from BaseUserDto to BaseUser]");
            }
        });
        mapperFactory.registerClassMap(userClassMapBuilder.use(BaseEntity.class, BaseEntityDto.class).byDefault().toClassMap());
        
        mapperFactory.registerClassMap(ClassMapBuilder.map(Customer.class, CustomerDto.class)
                .use(BaseUser.class, BaseUserDto.class)
                .byDefault()
                .toClassMap());
        
        mapperFactory.build();
        
        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        
        Customer customer = new Customer();
        customer.setId(new Long(1234));
        customer.setName("Customer Name");
        customer.setEmail("test@test.org");
        
        /* Map from Customer to CustomerDto works fine: */
        CustomerDto customerDto = mapperFacade.map(customer, CustomerDto.class);
        
        /*
         * Map from CustomerDto to Customer throws ClassCastException: Exception
         * in thread "main" ma.glasnost.orika.MappingException: Error
         * encountered while mapping for the following inputs:
         * rawSource=de.tib.ite
         * .gwtpoc.mapping.InheritanceTestcase$CustomerDto@5ed364c
         * sourceClass=class
         * de.tib.ite.gwtpoc.mapping.InheritanceTestcase$CustomerDto
         * sourceType=CustomerDto destinationType=Customer at
         * ma.glasnost.orika.impl
         * .MapperFacadeImpl.map(MapperFacadeImpl.java:278) at
         * ma.glasnost.orika.
         * impl.MapperFacadeImpl.map(MapperFacadeImpl.java:147) at
         * ma.glasnost.orika
         * .impl.MapperFacadeImpl.map(MapperFacadeImpl.java:622) at
         * de.tib.ite.gwtpoc
         * .mapping.InheritanceTestcase.testInheritance(InheritanceTestcase
         * .java:50) at de.tib.ite.gwtpoc.Main.main(Main.java:56) Caused by:
         * java.lang.ClassCastException:
         * de.tib.ite.gwtpoc.mapping.InheritanceTestcase$Customer cannot be cast
         * to de.tib.ite.gwtpoc.mapping. InheritanceTestcase$BaseUserDto at
         * ma.glasnost
         * .orika.generated.OrikaBaseUserDtoBaseUserMapper1254604688.mapBtoA
         * (OrikaBaseUserDtoBaseUserMapper1254604688.java:25) at
         * ma.glasnost.orika
         * .impl.GeneratedMapperBase.mapBtoA(GeneratedMapperBase.java:105) at
         * ma.
         * glasnost.orika.generated.OrikaCustomerDtoCustomerMapper2022515739.
         * mapBtoA (OrikaCustomerDtoCustomerMapper2022515739.java:23) at
         * ma.glasnost.orika
         * .impl.MapperFacadeImpl.mapDeclaredProperties(MapperFacadeImpl
         * .java:533) at
         * ma.glasnost.orika.impl.MapperFacadeImpl.map(MapperFacadeImpl
         * .java:264) ... 4 more
         */
        Customer c = mapperFacade.map(customerDto, Customer.class);
    }
    
    public static class BaseEntity {
        protected Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class BaseUser extends BaseEntity {
        protected String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class Customer extends BaseUser {
        protected String email;
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    public static class BaseEntityDto {
        protected Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }
    
    public static class BaseUserDto extends BaseEntityDto {
        protected String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    public static class CustomerDto extends BaseUserDto {
        protected String email;
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
}
