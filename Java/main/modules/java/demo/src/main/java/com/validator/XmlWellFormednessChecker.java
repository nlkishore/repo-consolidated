package com.validator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class XmlWellFormednessChecker {

    public static boolean isXmlWellFormed(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse the XML string to create a Document object
            Document document = builder.parse(new InputSource(new java.io.StringReader(xmlString)));
            
            return true; // If parsing succeeds, XML is well-formed
        } catch (Exception e) {
            // Catching any exceptions if XML is not well-formed
            return false;
        }
    }

    public static void main(String[] args) {
        String wellFormedXml = "<person><name>John</name><age>25</age></person>";
        String malformedXml = "<person><name>John</name><age>25</age></person>"; // Missing closing tag for "person"

        System.out.println("Is well-formed XML: " + isXmlWellFormed(wellFormedXml)); // Should print: true
        System.out.println("Is well-formed XML: " + isXmlWellFormed(malformedXml));   // Should print: false
    }
}

