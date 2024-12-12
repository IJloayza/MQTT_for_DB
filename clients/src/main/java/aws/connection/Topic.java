package aws.connection;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class Topic extends AWSIotTopic {
    public Topic(String topic,AWSIotQos qoso){
        super(topic, qoso);
    }

    @Override
    public void onMessage(AWSIotMessage message){
        String mPayload = message.getStringPayload();
        if(mPayload.contains("uid")){
            System.out.println("Message returned from AWS in Topic (" + message.getTopic() + "): " + mPayload);
            String uid = messageToUid(mPayload);
            System.out.println("Extracted UID: " + uid);
            boolean changes = sendMessageToBD(uid);
            ClientAws.publishAws(changes, (uid + " "));
        }
    }

    private String messageToUid(String message) {

        Pattern pattern = Pattern.compile("\"uid\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(message);
        String uid = "";
        if (matcher.find()) {
            uid = matcher.group(1);
        }
        return uid;  // Return: 53:cc:85:18
    }
    // This method use ClientDB to connect with database and create a uid
    private boolean sendMessageToBD(String uid){
        if(uid.isBlank())return false;
        boolean db = false;
        try {
            ClientDB.connect();
            db = ClientDB.addUID(uid);
            ClientDB.disconnect();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return db;
    }
}
