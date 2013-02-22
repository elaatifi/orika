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
    private Property.Builder[] elements = new Property.Builder[FieldElements.values().length];

    public ILoader init(ClassMapBuilder classMapBuilder) {
        Arrays.fill(elements, null);
        this.classMapBuilder = classMapBuilder;
        return this;
    }

    @Override
    public ILoader startElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        String name = event.asStartElement().getName().getLocalPart();
        FieldElements element = LoaderUtils.findLocalPart(name, FieldElements.class);
        return this;
    }

    @Override
    public ILoader character(MapperFactory factory, ILoader parent, XMLEvent event) {
        String data = event.asCharacters().getData();
        switch (element) {
            case A:
                elements[element.ordinal()] = Property.Builder.propertyFor(
                        classMapBuilder.getAType().getRawType(), data
                );
                break;
            case B:
                elements[element.ordinal()] = Property.Builder.propertyFor(
                    classMapBuilder.getAType().getRawType(), data
                );
                break;
            default:
                return super.character(factory, parent, event);
        }
        return this;
    }

    @Override
    public ILoader endElement(MapperFactory factory, ILoader parent, XMLEvent event) {
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
        return this;
    }
}
