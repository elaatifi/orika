package ma.glasnost.orika.metadata;

public class NestedProperty extends Property {
    
    private final Property[] path;
    private final String longGetter;
    
    public NestedProperty(Property property, Property[] path) {
        this.setType(property.getType());
        this.setGetter(property.getGetter());
        this.setSetter(property.getSetter());
        this.setParameterizedType(property.getParameterizedType());
        this.setName(property.getName());
        this.path = path;
        
        this.longGetter = getLongGetter(path);
    }
    
    private String getLongGetter(Property[] path) {
        StringBuilder sb = new StringBuilder();
        for (Property p : path) {
            sb.append(".").append(p.getGetter()).append("()");
        }
        sb.append(".").append(getGetter()).append("()");
        return sb.substring(1);
    }
    
    @Override
    public Property[] getPath() {
        return path;
    }
    
    @Override
    public boolean hasPath() {
        return true;
    }
    
    public String getLongGetter() {
        return longGetter;
    }
    
}
