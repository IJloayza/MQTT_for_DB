package aws.connection;

import java.sql.SQLException;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Topic extends AWSIotTopic {
    private static final ObjectMapper obj = new ObjectMapper();
    public Topic(String topic,AWSIotQos qoso){
        super(topic, qoso);
    }

    @Override
    public void onMessage(AWSIotMessage message){
        System.out.println("Message returned from AWS in Topic (" + message.getTopic() + "): " + message.getStringPayload());
        String uid = messageToString(message);
        System.out.println("Extracted UID: " + uid);
        sendMessageToBD(uid);
    }

    private String messageToString(AWSIotMessage message) {
        try {
            // Payload converted to String
            String payload = message.getStringPayload();

            // Transform the String in Json where we can find uid
            JsonNode jsonNode = obj.readTree(payload);
            //Returns the content in "uid" form the JSON
            return jsonNode.get("uid").asText();
        } catch (Exception e) {
            System.err.println("Error parsing message payload: " + e.getMessage());
            return null;
        }
    }
    // This method use ClientDB to connect with database and create a uid
    private void sendMessageToBD(String uid){
        try {
            ClientDB.connect();
            ClientDB.addUID(uid);
            ClientDB.disconnect();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
}
