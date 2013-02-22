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
    private FieldLoader fieldLoader;
    private ClassMapBuilder classMapBuilder;

    public MappingLoader(ILoader parent) {
        this.parent = parent;
        fieldLoader = new FieldLoader(this);
    }

    public ILoader init() {
        Arrays.fill(elements, null);
        return this;
    }

    @Override
    public ILoader startElement(MapperFactory factory, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        element = LoaderUtils.findLocalPart(name, MappingElements.class);
        if (element == MappingElements.FIELD) {
            if (!LoaderUtils.areAllNotNull(elements[MappingElements.CLASS_A.ordinal()],
                    elements[MappingElements.CLASS_B.ordinal()])) {
                log.error("Не заданы классы для мапинга {}-{}", elements[MappingElements.CLASS_A.ordinal()],
                        elements[MappingElements.CLASS_B.ordinal()]);
                throw new RuntimeException("Ошибка мапинга");
            }
            classMapBuilder = factory.classMap(elements[MappingElements.CLASS_A.ordinal()],
                    elements[MappingElements.CLASS_B.ordinal()]);
            return fieldLoader.init(classMapBuilder);
        }
        return this;
    }

    @Override
    public ILoader character(MapperFactory factory, XMLEvent event) {
        if (element == null) {
            return super.character(factory, event);
        }
        String data = event.asCharacters().getData();
        switch (element) {
            case CLASS_A:
            case CLASS_B:
                try {
                    elements[element.ordinal()] = Class.forName(data);
                } catch (ClassNotFoundException e) {
                    log.error("Class NOT Found " +  data);
                }
                break;
            default:
                return super.character(factory, event);
        }
        return this;
    }

    @Override
    public ILoader endElement(MapperFactory factory, XMLEvent event) {
        // если элемент класс и задана A и B, то добавить + сделать статус
        element = null;
        if (event.asEndElement().getName().getLocalPart().equals(MappingElements.MAPPING.getLocalPart())) {
            classMapBuilder.byDefault().register();
            return parent;
        }
        return super.endElement(factory, event);
    }
}
