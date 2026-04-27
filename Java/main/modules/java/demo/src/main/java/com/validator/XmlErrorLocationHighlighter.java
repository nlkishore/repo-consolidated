package com.validator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlErrorLocationHighlighter {

    public static boolean isXmlWellFormed(String xmlString) {

        System.out.println( "isXmlWellFormed Method "+xmlString);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Setting a custom ErrorHandler to capture parsing errors
            CustomErrorHandler errorHandler = new CustomErrorHandler();
            builder.setErrorHandler(errorHandler);

            // Parse the XML string to create a Document object
            Document document = builder.parse(new InputSource(new java.io.StringReader(xmlString)));

            // If parsing succeeds, XML is well-formed
            return true;
        } catch (SAXException se){
            se.printStackTrace();
            return false;
        } catch (Exception e) {
            // Catching any exceptions if XML is not well-formed
            //e.printStackTrace();
            return false;
        }
    }

    public static boolean isXmlWellFormedFromFile(String xmlFilePath) {
        System.out.println( "isXmlWellFormedFromFile Method "+xmlFilePath);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Setting a custom ErrorHandler to capture parsing errors
            CustomErrorHandler errorHandler = new CustomErrorHandler();
            builder.setErrorHandler(errorHandler);

            try{
            // Reading file in classpath , which is configured in config.properties
            InputStream is = XmlErrorLocationHighlighter.class.getResourceAsStream(xmlFilePath);
            // Parse the XML string to create a Document object
           // Document document = builder.parse(new FileInputStream(xmlFilePath));
           Document document = builder.parse(is);
            }catch (SAXException se){
                se.printStackTrace();
                return false;
            }
            // If parsing succeeds, XML is well-formed
            return true;
        }  catch (Exception e) {
            // Catching any exceptions if XML is not well-formed
            //e.printStackTrace();
            return false;
        }
    }
    public static void main(String[] args) {

        Properties properties = loadProperties("config.properties");
        if (properties == null) {
            System.out.println("Failed to load properties.");
            return;
        }

        String xmlFilePath = properties.getProperty("xml_file_path");
        System.out.println(xmlFilePath);
        String schemaFilePath = properties.getProperty("schema_file_path");
        System.out.println(schemaFilePath);

        if (xmlFilePath != null && schemaFilePath != null) {
          //  System.out.println("XML file path or schema file path not found in properties.");
          //  return;
          validateXMLagainstSchema(schemaFilePath,xmlFilePath);
        }

        
        //String malformedXml = "<person><name>John<name><age>25</age></person>"; // Missing closing tag for "person"

        //System.out.println("Is well-formed XML: " + isXmlWellFormed(malformedXml));
        System.out.println("Is well-formed XML: " +isXmlWellFormedFromFile(xmlFilePath));
    }

    static class CustomErrorHandler extends DefaultHandler {
        @Override
        public void error(SAXParseException e) {
            System.out.println("Parsing Error: " + e.getMessage());
            System.out.println("Error at line " + e.getLineNumber() + ", column " + e.getColumnNumber());
        }
    }

    @SuppressWarnings("unused")
    private static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        try (InputStream input = XmlErrorLocationHighlighter.class.getClassLoader().getResourceAsStream(filePath)) {
            properties.load(input);
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void validateXMLagainstSchema(String schemaFilePath,String xmlFilePath){

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
           
            InputStream schemaFileStream = XmlErrorLocationHighlighter.class.getResourceAsStream(schemaFilePath);
            Schema schema = factory.newSchema(new StreamSource(schemaFileStream);
            Validator validator = schema.newValidator();


            InputStream xmlFileStream = XmlErrorLocationHighlighter.class.getResourceAsStream(xmlFilePath);
          //  validator.validate(new StreamSource(new FileInputStream(xmlFilePath)));
            validator.validate(new StreamSource(xmlFileStream));
            System.out.println("Validation successful. XML is valid against the schema.");
        } catch (Exception e) {
            System.out.println("Validation failed. XML is not valid against the schema.");
            e.printStackTrace();
        }
    }
}