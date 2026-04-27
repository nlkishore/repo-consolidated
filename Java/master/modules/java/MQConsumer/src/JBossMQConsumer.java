import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

public class JBossMQConsumer {
    public static void main(String[] args) {
        try {
            // Load properties from config folder inside the classpath
            Properties properties = new Properties();
            InputStream input = JBossMQConsumer.class.getClassLoader().getResourceAsStream("config/config.properties");
            if (input == null) {
                System.err.println("Error: Unable to find config.properties");
                return;
            }
            properties.load(input);

            // Read JNDI & Queue details
            String jndiFactory = properties.getProperty("jndi.factory");
            String jndiUrl = properties.getProperty("jndi.url");
            String connectionFactoryName = properties.getProperty("jms.connection.factory");
            String queueName = properties.getProperty("jms.queue.name");
            String username = properties.getProperty("jms.username");
            String password = properties.getProperty("jms.password");

            // Set up JNDI environment
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
            env.put(Context.PROVIDER_URL, jndiUrl);

            Context context = new InitialContext(env);
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(connectionFactoryName);
            Queue queue = (Queue) context.lookup(queueName);

            // Create JMS connection, session, and consumer
            Connection connection = connectionFactory.createConnection(username, password);
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(queue);

            System.out.println("Waiting for messages...");

            while (true) {
                Message message = consumer.receive(5000); // Wait 5 seconds for a message

                if (message == null) {
                    System.out.println("No messages found.");
                    break;
                }

                // Process different message types
                if (message instanceof TextMessage) {
                    System.out.println("Received Text Message: " + ((TextMessage) message).getText());
                } else if (message instanceof BytesMessage) {
                    BytesMessage bytesMessage = (BytesMessage) message;
                    byte[] data = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(data);
                    System.out.println("Received Bytes Message: " + new String(data));
                } else if (message instanceof ObjectMessage) {
                    System.out.println("Received Object Message: " + ((ObjectMessage) message).getObject());
                } else {
                    System.out.println("Received unknown message type: " + message);
                }
            }

            // Cleanup
            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
