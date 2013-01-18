package edu.depaul.se560;

import java.io.StringReader;


import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


public class StaxParser {
	private String element; 
	private String text; 
	private StringReader xHtml;

	public StaxParser(String elem, String txt, StringReader sr) {
		element = elem; 
		text = txt; 
		xHtml = sr; 
	}

	public boolean parse() throws XMLStreamException {
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		/*Set this property to avoid exceptions due to long CDATA blocks
		 * See http://jira.codehaus.org/browse/WSTX-211 for more details
		 * Supposedly the bug has been fixed in v4.0.5 of WoodStox
		 */
		inputFactory.setProperty(inputFactory.IS_COALESCING, true);
		XMLStreamReader streamReader = inputFactory.createXMLStreamReader(xHtml);

		while(streamReader.hasNext()) {
			streamReader.next();
			if(streamReader.getEventType() == XMLStreamReader.CDATA) {
				continue;
			}
			if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
				if(element == streamReader.getElementText() && streamReader.getText().contains(text)) {
					return true;
				}

				if(element == streamReader.getElementText() && !streamReader.getText().contains(text)) {
					while(streamReader.hasNext()) {
						if(streamReader.getEventType() == XMLStreamReader.CDATA) {
							continue;
						}
						if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT) {
							if(streamReader.getText().contains(text)) {
								return true;
							}
						}
					}

				}
			}
		}
		return false;
	}
}
