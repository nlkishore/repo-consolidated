import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class XMLTransformExample {
    public static void main(String[] args) {
        try {
            // Instantiate a TransformerFactory
            TransformerFactory factory = new TransformerFactoryImpl();

            // Load the XSLT file
            Source xslt = new StreamSource(new File("transform.xslt"));

            // Compile the stylesheet
            Transformer transformer = factory.newTransformer(xslt);

            // Load the input XML document
            Source xml = new StreamSource(new File("input.xml"));

            // Set the output
            Result output = new StreamResult(new File("output.xml"));

            // Perform the transformation
            transformer.transform(xml, output);

            System.out.println("Transformation completed successfully.");
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }
}

