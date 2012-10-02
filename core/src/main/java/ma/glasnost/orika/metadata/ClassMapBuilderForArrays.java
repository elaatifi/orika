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

package ma.glasnost.orika.metadata;

import ma.glasnost.orika.DefaultFieldMapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * ClassMapBuilderForMaps is a custom ClassMapBuilder instance which is
 * used for mapping standard JavaBeans to Map instances.
 *
 * @param <A>
 * @param <B>
 */
public class ClassMapBuilderForArrays<A, B> extends ClassMapBuilderForLists<A,B> {
    
	
	public static class Factory extends ClassMapBuilderFactory {

		/* (non-Javadoc)
		 * @see ma.glasnost.orika.metadata.ClassMapBuilderFactory#newClassMapBuilder(ma.glasnost.orika.metadata.Type, ma.glasnost.orika.metadata.Type, ma.glasnost.orika.property.PropertyResolverStrategy, ma.glasnost.orika.DefaultFieldMapper[])
		 */
        @Override
		protected <A, B> ClassMapBuilder<A,B> newClassMapBuilder(
				Type<A> aType, Type<B> bType,
				MapperFactory mapperFactory,
				PropertyResolverStrategy propertyResolver,
				DefaultFieldMapper[] defaults) {
			
			return new ClassMapBuilderForArrays<A,B>(aType, bType, mapperFactory, propertyResolver, defaults);
		}
	}
	
    /**
     * @param aType
     * @param bType
     * @param propertyResolver
     * @param defaults
     */
    protected ClassMapBuilderForArrays(Type<A> aType, Type<B> bType, MapperFactory mapperFactory, PropertyResolverStrategy propertyResolver, DefaultFieldMapper... defaults) {
	    super(aType, bType, mapperFactory, propertyResolver, defaults);
	}
       
    protected ClassMapBuilderForArrays<A, B> self() {
        return this;
    }           
    
    protected boolean isATypeBean() {
        return !getAType().isArray();
    }
    
    protected boolean isSpecialCaseType(Type<?> type) {
        return type.isArray();
    }
     
    protected Property resolveCustomProperty(String expr, Type<?> propertyType) {
        return new ArrayElementProperty(expr, propertyType.getComponentType());
    }
    
    /**
     * ListElementProperty is a special Property instance used to represent a value
     * which associated with a particular index in a list.
     * 
     * @author matt.deboer@gmail.com
     *
     */
    public static final class ArrayElementProperty extends Property {
        
        public ArrayElementProperty(String indexExpr, Type<?> propertyType) {
            indexExpr = indexExpr.replaceAll("[\\[\\]]", "");
            int index = Integer.valueOf(indexExpr);
            setName("["+index + "]");
            setExpression("["+index + "]");
            setGetter("[" + index + "]");
            setSetter("[" + index + "] = %s");
            setType(propertyType);
        }
        
        public boolean isArrayElement() {
            return true;
        }
    }
}
