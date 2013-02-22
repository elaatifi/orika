package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import ma.glasnost.orika.metadata.Property;

import javax.xml.stream.events.XMLEvent;
import java.text.MessageFormat;
import java.util.Arrays;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class FieldLoader extends DefaultLoader {
    private FieldElements element;
    private ClassMapBuilder classMapBuilder;
    private String[] elements = new String[FieldElements.values().length];

    public FieldLoader(ILoader parent) {
        this.parent = parent;
    }

    public ILoader init(ClassMapBuilder classMapBuilder) {
        Arrays.fill(elements, null);
        this.classMapBuilder = classMapBuilder;
        return this;
    }

    @Override
    public ILoader startElement(MapperFactory factory, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        element = LoaderUtils.findLocalPart(name, FieldElements.class);
        return this;
    }

    @Override
    public ILoader character(MapperFactory factory, XMLEvent event) {
        if (element == null) {
            return super.character(factory, event);
        }
        String data = event.asCharacters().getData();
        switch (element) {
            case A:
                elements[element.ordinal()] = data;
/*
                Property.Builder.propertyFor(
                        classMapBuilder.getAType().getRawType(), data
                );
*/
                break;
            case B:
                elements[element.ordinal()] = data;
            default:
                return super.character(factory, event);
        }
        return this;
    }

    @Override
    public ILoader endElement(MapperFactory factory, XMLEvent event) {
        if (element == null) {
            if (LoaderUtils.areAllNotNull(elements[FieldElements.A.ordinal()], elements[FieldElements.B.ordinal()]) ) {
                classMapBuilder.field(elements[FieldElements.A.ordinal()], elements[FieldElements.B.ordinal()]);
                log.info(MessageFormat.format("Для мапинга {0}-{1} добавлены поля a-{2} b-{3}",
                        classMapBuilder.getAType().getName(), classMapBuilder.getBType().getName(),
                        elements[FieldElements.A.ordinal()].toString(), elements[FieldElements.B.ordinal()].toString()));
                return parent;
            } else {
                log.error(MessageFormat.format("Для мапинга {0}-{1} не заполнены поля a-{2} b-{3}",
                        classMapBuilder.getAType().getName(), classMapBuilder.getBType().getName(),
                        elements[FieldElements.A.ordinal()].toString(), elements[FieldElements.B.ordinal()].toString()));
            }
            return parent;
        }
        element = null;
        return this;
    }
}
