package ma.glasnost.orika.metadata;

import ma.glasnost.orika.impl.Specifications.Specification;

public class FieldMap {
    
    private final Property source;
    private final Property destination;
    private boolean excluded;
    private final boolean configured;
    
    public FieldMap(Property a, Property b) {
        this(a, b, false, false);
    }
    
    public FieldMap(Property a, Property b, boolean configured, boolean excluded) {
        this.source = a;
        this.destination = b;
        this.excluded = excluded;
        this.configured = configured;
    }
    
    public boolean isExcluded() {
        return excluded;
    }
    
    public void setExcluded(boolean exclude) {
        this.excluded = exclude;
    }
    
    public Property getSource() {
        return source;
    }
    
    public Property getDestination() {
        return destination;
    }
    
    String getSourceName() {
        return source.getName();
    }
    
    String getDestinationName() {
        return destination.getName();
    }
    
    public boolean isConfigured() {
        return configured;
    }
    
    public FieldMap flip() {
        return new FieldMap(destination, source, configured, excluded);
    }
    
    public boolean is(Specification specification) {
        return specification.apply(this);
    }
    
    public boolean have(Specification specification) {
        return specification.apply(this);
    }
    
    @Override
    public String toString() {
        return "FieldMap [destination=" + getDestinationName() + ", source=" + getSourceName() + "]";
    }
    
}
