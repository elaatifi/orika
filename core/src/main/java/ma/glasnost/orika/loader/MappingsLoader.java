package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;

import javax.xml.stream.events.XMLEvent;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class MappingsLoader extends DefaultLoader {

    private static final String MAPPING = "mapping";
    private MappingLoader mappingLoader;

    public MappingsLoader() {
        mappingLoader = new MappingLoader(this);
        parent = this;
    }

    @Override
    public ILoader startElement(MapperFactory factory, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        if (name.equals(MAPPING)) {
            return mappingLoader.init();
        }
        return super.startElement(factory, event);
    }

    @Override
    public ILoader character(MapperFactory factory, XMLEvent event) {
        return super.character(factory, event);
    }

    @Override
    public ILoader endElement(MapperFactory factory, XMLEvent event) {
        return super.endElement(factory, event);
    }
}
