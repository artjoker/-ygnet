package com.cygnet.ourdrive.swingTail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class XMLUtils {

	private static DocumentBuilder documentBuilder;
	private static DocumentBuilder namespaceAwareDocumentBuilder;

	static{
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

			documentBuilder = documentBuilderFactory.newDocumentBuilder();

			documentBuilderFactory.setNamespaceAware(true);

			namespaceAwareDocumentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

    public static Document createDomDocument(){

    	return documentBuilder.newDocument();
    }

    public static Document createNamespaceAwareDomDocument(){

    	return namespaceAwareDocumentBuilder.newDocument();
    }

    public static String toString(Document doc, String encoding, boolean indent) throws TransformerFactoryConfigurationError, TransformerException {
        Source source = new DOMSource(doc);
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.ENCODING,encoding);

        if(indent){
            xformer.setOutputProperty(OutputKeys.INDENT,"yes");
        }

        xformer.transform(source, result);

        return sw.getBuffer().toString();
    }

    public static void toString(Document doc,String encoding, Writer w, boolean indent) throws TransformerFactoryConfigurationError, TransformerException {
        Source source = new DOMSource(doc);
        Result result = new StreamResult(w);

        Transformer xformer = TransformerFactory.newInstance().newTransformer();
        xformer.setOutputProperty(OutputKeys.ENCODING,encoding);

        if(indent){
            xformer.setOutputProperty(OutputKeys.INDENT,"yes");
        }

        xformer.transform(source, result);
    }

    public static Document parseXmlFile(String filename, boolean validating) throws SAXException, IOException, ParserConfigurationException {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);
            Document doc = factory.newDocumentBuilder().parse(new File(filename));
            return doc;
    }

    public static Document parseXmlFile(File f, boolean validating) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);
        Document doc = factory.newDocumentBuilder().parse(f);
        return doc;
    }

    public static Document parseXmlFile(InputStream stream, boolean validating) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(validating);
        Document doc = factory.newDocumentBuilder().parse(stream);
        return doc;
    }

    public static Element createElement(String name, String value, Document doc){
    	Element element = doc.createElement(name);
    	element.appendChild(doc.createTextNode(value));
    	return element;
    }

    public static Element createCDATAElement(String name, String value, Document doc){
    	Element element = doc.createElement(name);
    	element.appendChild(doc.createCDATASection(value));
    	return element;
    }

    public static void writeXmlFile(Document doc, String filename, boolean indent) throws TransformerFactoryConfigurationError, TransformerException {
        // Prepare the DOM document for writing
        Source source = new DOMSource(doc);

        // Prepare the output file
        File file = new File(filename);
        Result result = new StreamResult(file);

        // Write the DOM document to the file
        Transformer xformer = TransformerFactory.newInstance().newTransformer();

        if(indent){
            xformer.setOutputProperty(OutputKeys.INDENT,"yes");
        }

        xformer.transform(source, result);
    }
}
