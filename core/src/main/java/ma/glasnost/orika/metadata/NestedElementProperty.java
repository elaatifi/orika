package ma.glasnost.orika.metadata;

import ma.glasnost.orika.property.PropertyResolverStrategy;

/**
 * NestedElementProperty represents a property which belongs to the type of a nested list/array/map element
 * within another type.
 * 
 * @author mattdeboer
 *
 */
public class NestedElementProperty extends Property {

    private final Property container;
    
    /**
     * @param owningProperty
     * @param elementProperty
     * @param resolver 
     */
    public NestedElementProperty(Property owningProperty, Property elementProperty, PropertyResolverStrategy resolver) {
        super(getElementExpression(owningProperty, elementProperty), 
                elementProperty.getName(), elementProperty.getGetter(), elementProperty.getSetter(),
                elementProperty.getType(), elementProperty.getElementType());
        this.container = initContainer(owningProperty, elementProperty.getExpression(), resolver);
    }
    
    /**
     * Returns the element expression for this property
     * 
     * @return the element expression for this property
     */
    private static String getElementExpression(Property owningProperty, Property elementProperty) {
        return owningProperty.getExpression() + "[" + elementProperty.getExpression() + "]";
    }
    
    private static Property initContainer(Property owningProperty, String propertyExpression, PropertyResolverStrategy resolver) {
        String[] parts = propertyExpression.replace("]","").split("\\[");
        if (parts.length > 1) {
            StringBuilder containerExpression = new StringBuilder("");
            boolean nested = false;
            for (int i = parts.length -2; i >= 0; --i) {
                String part = parts[i];
                    containerExpression.insert(0, "[" + part);
                    containerExpression.append("]");
                nested = true;
            }
            return resolver.getProperty(owningProperty.getType(), containerExpression.toString());
        } else {
            return owningProperty;
        }
    }
    
    /**
     * @return the containing property of this NestedElementProperty
     */
    public Property getContainer() {
        return container;
    }
    
    /**
     * @return the root container of this property
     */
    public Property getRootContainer() {
        Property container = this.container;
        while (container.getContainer() != null) {
            container = container.getContainer();
        }
        return container;
    }
}
