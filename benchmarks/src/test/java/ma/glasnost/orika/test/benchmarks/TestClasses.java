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
package ma.glasnost.orika.test.benchmarks;

import java.util.ArrayList;
import java.util.List;

import com.inspiresoftware.lib.dto.geda.annotations.Dto;

/**
 * @author matt.deboer@gmail.com
 *
 */
public abstract class TestClasses {
	
	public static class Product {
		private String productName;
	    private String productDescription;
	    private Double price;
	    private Boolean availability;
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getProductDescription() {
			return productDescription;
		}
		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Boolean getAvailability() {
			return availability;
		}
		public void setAvailability(Boolean availability) {
			this.availability = availability;
		} 
	}
	
	
	public static class ProductDto {
		private String productName;
	    private String productDescription;
	    private Double price;
	    private Boolean availability;
		public String getProductName() {
			return productName;
		}
		public void setProductName(String productName) {
			this.productName = productName;
		}
		public String getProductDescription() {
			return productDescription;
		}
		public void setProductDescription(String productDescription) {
			this.productDescription = productDescription;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Boolean getAvailability() {
			return availability;
		}
		public void setAvailability(Boolean availability) {
			this.availability = availability;
		}   
	}
	
	@Dto
	public static class One {
        public List<Two> getTwos() {
            return twos;
        }
        
        public void setTwos(List<Two> twos) {
            this.twos = twos;
        }
        
        List<Two> twos = new ArrayList<Two>();
        
        public One(String name) {
            this.name = name;
            twos.add(new Two(name));
        }
        
        public One() {
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        String name;
        
    }
    
	@Dto
    public static class Two {
        public Two() {
        }
        
        public Two(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        String name;
    }
    
    @Dto
    public static class Parent {
        
    	public String productName;
    	public String productDescription;
    	public Double price;
    	public Boolean availability;
    	
    	public List<One> getOneList() {
            return oneList;
        }
        
        public void setOneList(List<One> oneList) {
            this.oneList = oneList;
        }
        
        List<One> oneList = new ArrayList<One>();
        
        public List<Two> getTwoList() {
            return twoList;
        }
        
        public void setTwoList(List<Two> twoList) {
            this.twoList = twoList;
        }
        
        List<Two> twoList = new ArrayList<Two>();
    }
    
    public static Parent createParent() {
    	
        Parent parent = new Parent();
        parent.availability = true;
        parent.price = 123d;
        parent.productDescription = "desc";
        parent.productName ="name";
        return parent;

    }
    
    public static Parent createParentWithChildren() {
    	
        Parent parent = createParent();
        List<One> ones = new ArrayList<One>();
        List<Two> twos = new ArrayList<Two>();
        for (int j = 0; j < 50; j++) {
            
            One one = new One(Integer.toString(j));
            for (int k = 0; k < 50; k++) {
            	one.twos.add(new Two(Integer.toString(k)));
            }
            ones.add(one);
            twos.add(new Two(Integer.toString(j)));
        }
        
        parent.oneList = ones;
        parent.twoList = twos;
       
        return parent;

    }
}
