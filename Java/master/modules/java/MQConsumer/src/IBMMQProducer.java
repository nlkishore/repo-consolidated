import com.ibm.mq.*;

public class IBMMQProducer {
    public static void main(String[] args) {
        try {
            // Set up the connection to MQ
            MQQueueManager queueManager = new MQQueueManager("QM1");
            MQQueue queue = queueManager.accessQueue("XYZ_QUEUE", MQQueueManager.MQOO_OUTPUT);

            // Create a message to send
            MQMessage message = new MQMessage();
            message.writeString("Hello, this is a test message for MQ!");

            // Define the options for the message
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            // Put the message into the queue
            queue.put(message, pmo);
            System.out.println("Message Sent to MQ!");

            // Clean up
            queue.close();
            queueManager.disconnect();
        } catch (MQException e) {
            e.printStackTrace();
        }
    }
}
