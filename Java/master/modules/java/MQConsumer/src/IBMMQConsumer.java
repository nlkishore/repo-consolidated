import com.ibm.mq.*;

public class IBMMQConsumer {
    public static void main(String[] args) {
        try {
            // Set up the connection to MQ
            MQQueueManager queueManager = new MQQueueManager("QM1");
            MQQueue queue = queueManager.accessQueue("XYZ_QUEUE", MQQueueManager.MQOO_INPUT_AS_Q_DEF);

            // Create a message to receive
            MQMessage message = new MQMessage();

            // Define the options for receiving the message
            MQGetMessageOptions gmo = new MQGetMessageOptions();

            // Get the message from the queue
            queue.get(message, gmo);
            String receivedMessage = message.readString(message.getMessageLength());
            System.out.println("Received Message: " + receivedMessage);

            // Clean up
            queue.close();
            queueManager.disconnect();
        } catch (MQException e) {
            e.printStackTrace();
        }
    }
}
