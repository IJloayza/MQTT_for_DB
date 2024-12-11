package aws.connection;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

/**
 * This class extends {@link AWSIotMessage} to provide customized handlers for
 * non-blocking message publishing.
 */
public class NonBlockingPublishListener extends AWSIotMessage {

    public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
        super(topic, qos, payload);
    }

    @Override
    public void onSuccess() {
        return;
    }

    @Override
    public void onFailure() {
        System.out.println(System.currentTimeMillis() + ": publish failed for " + getStringPayload());
    }

    @Override
    public void onTimeout() {
        System.out.println(System.currentTimeMillis() + ": publish timeout for " + getStringPayload());
    }

}