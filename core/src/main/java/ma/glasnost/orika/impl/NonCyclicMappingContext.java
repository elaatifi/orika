/*
 * Orika - simpler, better and faster Java bean mapping
 *
 * Copyright (C) 2011-2013 Orika authors
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

package ma.glasnost.orika.impl;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.MappingContextFactory;
import ma.glasnost.orika.metadata.Type;

/**
 * @author mattdeboer
 *
 */
public class NonCyclicMappingContext extends MappingContext {

    /**
     * Factory constructs instances of the base MappingContext
     */
    public static class Factory implements MappingContextFactory {
        
        final LinkedBlockingQueue<MappingContext> contextQueue = new LinkedBlockingQueue<MappingContext>();
        final Map<Object, Object> globalProperties;
        
        /**
         * @param globalProperties
         */
        public Factory(Map<Object, Object> globalProperties) {
            this.globalProperties = globalProperties;
        }
        
        public MappingContext getContext() {
            MappingContext context = contextQueue.poll();
            if (context == null) {
                context = new NonCyclicMappingContext(globalProperties);
            }
            context.containsCycle(false);
            return context;
        }
        
        public void release(MappingContext context) {
            context.reset();
            contextQueue.offer(context);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see ma.glasnost.orika.MappingContextFactory#getGlobalProperties()
         */
        public Map<Object, Object> getGlobalProperties() {
            return globalProperties;
        }
    }
    
    
    /**
     * @param globalProperties
     */
    protected NonCyclicMappingContext(Map<Object, Object> globalProperties) {
        super(globalProperties);
    }

    @Override
    public <S, D> void cacheMappedObject(S source, Type<Object> destinationType, D destination) {
        // NO-OP
    }

    @Override
    public <D> D getMappedObject(Object source, Type<?> destinationType) {
        return null;
    }
}
