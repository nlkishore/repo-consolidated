package com.example;

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.turbine.TurbineConfig;
import org.apache.turbine.util.TurbineException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize Turbine
            TurbineConfig tc = new TurbineConfig("src/main/resources", "TurbineResources.properties");
            tc.init();

            // Parse XML to Java Object
            File file = new File("src/main/resources/data.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Transaction.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Transaction transaction = (Transaction) jaxbUnmarshaller.unmarshal(file);

            // Initialize Hibernate
            SessionFactory factory = new Configuration().configure().buildSessionFactory();
            Session session = factory.openSession();
            session.beginTransaction();

            // Persist the transaction object
            session.save(transaction);
            session.getTransaction().commit();

            // Display the persisted transaction
            System.out.println("Transaction persisted: " + transaction.getDescription());

            // Close the session and Turbine
            session.close();
            factory.close();
            tc.shutdown();
        } catch (TurbineException | ConfigurationException | JAXBException | IOException e) {
            e.printStackTrace();
        }
    }
}
