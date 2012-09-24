/*
 * Orika - simpler, better and faster Java bean mapping
 * 
 * Copyright (C) 2011 Orika authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.test.property;

import junit.framework.Assert;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.property.RegexPropertyResolver;

import org.junit.Test;

/**
 * @author matt.deboer@gmail.com
 *
 */
public class RegexPropertyResolverTestCase {
    
    public static class A {
        private Name name;
        private Address address;
        
        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Name readTheNameForThisBean() {
            return name;
        }

        public void assignTheName(Name name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((address == null) ? 0 : address.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            A other = (A) obj;
            if (address == null) {
                if (other.address != null)
                    return false;
            } else if (!address.equals(other.address))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        } 
        
        
    }
    
    public static class B {
        public String givenName;
        public String sirName;
        public String street;
        public String city;
        public String postalCode;
        public String country;
    }
    
    public static class Name {
        private String firstName;
        private String lastName;
        
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
            result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Name other = (Name) obj;
            if (firstName == null) {
                if (other.firstName != null)
                    return false;
            } else if (!firstName.equals(other.firstName))
                return false;
            if (lastName == null) {
                if (other.lastName != null)
                    return false;
            } else if (!lastName.equals(other.lastName))
                return false;
            return true;
        }
        
    }
    
    public static class Address {
        public String street;
        public String city;
        public String postalCode;
        public String country;
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((city == null) ? 0 : city.hashCode());
            result = prime * result + ((country == null) ? 0 : country.hashCode());
            result = prime * result + ((postalCode == null) ? 0 : postalCode.hashCode());
            result = prime * result + ((street == null) ? 0 : street.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Address other = (Address) obj;
            if (city == null) {
                if (other.city != null)
                    return false;
            } else if (!city.equals(other.city))
                return false;
            if (country == null) {
                if (other.country != null)
                    return false;
            } else if (!country.equals(other.country))
                return false;
            if (postalCode == null) {
                if (other.postalCode != null)
                    return false;
            } else if (!postalCode.equals(other.postalCode))
                return false;
            if (street == null) {
                if (other.street != null)
                    return false;
            } else if (!street.equals(other.street))
                return false;
            return true;
        }
        
    }
    
    @Test
    public void testRegexResolution() {
        
        MapperFactory factory = 
                new DefaultMapperFactory.Builder()
                    .propertyResolverStrategy(
                            new RegexPropertyResolver(
                                    "readThe([\\w]+)ForThisBean",
                                    "assignThe([\\w]+)",
                                    true, true))
                    .build();
        factory.registerClassMap(
                factory.classMap(A.class, B.class)
                    .field("name.firstName", "givenName")
                    .field("name.lastName", "sirName")
                    .field("address.city", "city")
                    .field("address.street", "street")
                    .field("address.postalCode", "postalCode")
                    .field("address.country", "country")
                );
        
        
        MapperFacade mapper = factory.getMapperFacade();
        
        A a = new A();
        Name name = new Name();
        name.setFirstName("Albert");
        name.setLastName("Einstein");
        a.assignTheName(name);
        Address address = new Address();
        address.city = "Somewhere";
        address.country = "Germany";
        address.postalCode = "A1234FG";
        address.street = "1234 Easy St.";
        a.setAddress(address);
        
        
        B b = mapper.map(a, B.class);
        
        Assert.assertNotNull(b);
        
        A mapBack = mapper.map(b, A.class);
        
        Assert.assertEquals(a, mapBack);
        
    }
    
}
