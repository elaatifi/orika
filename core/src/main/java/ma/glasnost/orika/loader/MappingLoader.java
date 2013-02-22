package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;

import javax.xml.stream.events.XMLEvent;
import java.util.Arrays;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class MappingLoader extends DefaultLoader {
    private MappingElements element;
    private Class<?>[] elements = new Class<?>[MappingElements.values().length];
    private FieldLoader fieldLoader = new FieldLoader();
    private ClassMapBuilder classMapBuilder;

    public ILoader init() {
        Arrays.fill(elements, null);
        return this;
    }

    @Override
    public ILoader startElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        element = LoaderUtils.findLocalPart(name, MappingElements.class);
        return this;
    }

    @Override
    public ILoader character(MapperFactory factory, ILoader parent, XMLEvent event) {
        String data = event.asCharacters().getData();
        switch (element) {
            case CLASS_A:
            case CLASS_B:
                try {
                    elements[element.ordinal()] = Class.forName(data);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case FIELD:
                if (!LoaderUtils.areAllNotNull(elements)) {
                    log.error("Не заданы классы для мапинга {}-{}", elements[MappingElements.CLASS_A.ordinal()],
                        elements[MappingElements.CLASS_B.ordinal()]);
                    throw new RuntimeException("Ошибка мапинга");
                }
                classMapBuilder = factory.classMap(elements[MappingElements.CLASS_A.ordinal()],
                        elements[MappingElements.CLASS_B.ordinal()]);
                return fieldLoader.init(classMapBuilder);
            default:
                return super.character(factory, parent, event);
        }
        return this;
    }
}
