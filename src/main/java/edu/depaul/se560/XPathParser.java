package edu.depaul.se560;

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

public class XPathParser {

	private String expression;
	private InputSource xHtml;

	
	public XPathParser(String xPression,InputSource html) {
		expression = xPression;
		xHtml = html;
	}

	public boolean evaluate() throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xHtml);
		
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(expression);
		
		NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

		if(nodes.getLength() > 0) {
			return true;
		}
		
		return false;
		

	}

	public static boolean validXPression (String expression) {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			XPathExpression expr = xpath.compile(expression);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

}
