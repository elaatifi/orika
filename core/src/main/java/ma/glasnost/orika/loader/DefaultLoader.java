package ma.glasnost.orika.loader;

import ma.glasnost.orika.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

/**
 * @author  ikozar
 * date     13.02.13
 */
public class DefaultLoader implements ILoader {
    protected static Logger log;

    public DefaultLoader() {
        log = LoggerFactory.getLogger(this.getClass());
    }

    public ILoader startElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        System.out.print("START_ELEMENT " + event.asStartElement().getName() +
                ", att: ");
        for (Iterator it = event.asStartElement().getAttributes(); it.hasNext(); ) {
            Attribute att = (Attribute) it.next();
            System.out.print(att.getName().getLocalPart() + '=' + att.getValue() + ' ');
        }
        System.out.println("");
        return this;
    }

    public ILoader character(MapperFactory factory, ILoader parent, XMLEvent event) {
        System.out.println("CHARACTERS " + event.asCharacters().getData());
        return this;
    }

    public ILoader endElement(MapperFactory factory, ILoader parent, XMLEvent event) {
        System.out.println("END_ELEMENT " + event.asEndElement().getName());
        return parent;
    }
}
