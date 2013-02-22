package ma.glasnost.orika.loader;

/**
* @author ikozar
* date     13.02.13
*/
enum FieldElements implements ILocalPart {
    A("a"), B("b");
    private String localPart;

    FieldElements(String localPart) {
        this.localPart = localPart;
    }

    public String getLocalPart() {
        return localPart;
    }
}
