package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class MappingsLoader extends DefaultLoader {

    private static final String MAPPING = "mapping";
    private MappingLoader mappingLoader = new MappingLoader();

    @Override
    public ILoader startElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        if (name.equals(MAPPING)) {
            return mappingLoader.init();
        }
        return super.startElement(factory, parent, event);
    }

    @Override
    public ILoader character(MapperFactory factory, ILoader parent, XMLEvent event) {
        return super.character(factory, parent, event);
    }

    @Override
    public ILoader endElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        return super.endElement(factory, parent, event);
    }
}
