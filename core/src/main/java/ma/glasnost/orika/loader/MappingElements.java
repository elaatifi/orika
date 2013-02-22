package ma.glasnost.orika.loader;

/**
* @author ikozar
* date     13.02.13
*/
enum MappingElements implements ILocalPart {
    CLASS_A("class-a"), CLASS_B("class-b"), EXCLUDE("exclude"), FIELD("field"), MAPPING("mapping");
    private String localPart;

    MappingElements(String localPart) {
        this.localPart = localPart;
    }

    public String getLocalPart() {
        return localPart;
    }
}
