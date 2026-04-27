package org.example;

import net.sf.saxon.s9api.*;

import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class XSLTTransformation1 {
    public static void main(String[] args) {
        try {
            // Load the XSLT file
            Processor processor = new Processor(false);
            XsltCompiler compiler = processor.newXsltCompiler();
            XsltExecutable exec = compiler.compile(new StreamSource(new File("src/main/resources/transform1.xslt")));

            // Load the input XML document
            XdmNode source = processor.newDocumentBuilder().build(new StreamSource(new File("src/main/resources/input1.xml")));

            // Set up the transformer
            XsltTransformer transformer = exec.load();

            // Set the source document and the result output
            transformer.setInitialContextNode(source);
            Serializer out = processor.newSerializer(new File("src/main/resources/output1.xml"));
            transformer.setDestination(out);

            // Call the Java method and pass the result as a parameter
            StringUtils utils = new StringUtils();
            String upperCaseText = utils.toUpperCase("Hello, World!");
            transformer.setParameter(new QName("upperCaseText"), new XdmAtomicValue(upperCaseText));

            // Perform the transformation
            transformer.transform();

            System.out.println("Transformation completed successfully.");
        } catch (SaxonApiException e) {
            e.printStackTrace();
        }
    }
}
