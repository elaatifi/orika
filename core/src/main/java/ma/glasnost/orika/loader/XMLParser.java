/*
 * Copyright 2005-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ma.glasnost.orika.loader;


import com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;
import ma.glasnost.orika.MapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventAllocator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Stack;

/**
 * Internal class that parses a raw custom xml mapping file into Map objects.
 */
public class XMLParser {

    private static final Logger log = LoggerFactory.getLogger(XMLParser.class);

    private XMLEventAllocator allocator;
    private MapperFactory factory;

    public XMLParser(MapperFactory factory) {
        this.factory = factory;
    }

    public void reader(String filename) {
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            System.out.println("FACTORY: " + xmlif);
            xmlif.setEventAllocator(new XMLEventAllocatorImpl());
            allocator = xmlif.getEventAllocator();
            XMLStreamReader xmlr = xmlif.createXMLStreamReader(filename,
                    new FileInputStream(filename));

            // when XMLStreamReader is created,
            // it is positioned at START_DOCUMENT event.
            int eventType = xmlr.getEventType();
            ILoader loader = new MappingsLoader();
            Stack<ILoader> loaderStack = new Stack<ILoader>();
            loaderStack.push(loader);
/*
            printEventType(eventType);
            printStartDocument(xmlr);
*/

            // check if there are more events
            // in the input stream
            while(xmlr.hasNext()) {
                eventType = xmlr.next();
                XMLEvent event = allocator.allocate(xmlr);
                if (eventType == XMLStreamConstants.START_ELEMENT) {
                    loader = loader.startElement(factory, event);
                } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                    loader = loader.endElement(factory, event);
                } else if (eventType == XMLStreamConstants.CHARACTERS) {
                    loader = loader.character(factory, event);
                } else if (eventType == XMLStreamConstants.END_DOCUMENT) {
                    System.out.println("END_DOCUMENT ");
                } else {
                    System.out.println(eventType);
                }

                System.out.println(loader.getClass().getSimpleName());

//                printEventType(eventType);

                // these functions print the information
                // about the particular event by calling
                // the relevant function
/*
                printStartElement(xmlr);
                printEndElement(xmlr);
                printText(xmlr);
                printPIData(xmlr);
                printComment(xmlr);
*/
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
