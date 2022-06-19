package in.eatie;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;


public class MQTT {
    MemoryPersistence persistence = new MemoryPersistence();
    MqttClient client;
    private String broker;
    private String clientId;

    MQTT(String broker, String clientId) {
        this.broker = broker;
        this.clientId = clientId;
    }

    public static MqttConnectOptions getConnectionOptions(JSONObject options) throws JSONException {
        int keepAliveInterval = 5;
        boolean cleanSession = false;
        boolean automaticReconnect = true;
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setKeepAliveInterval(keepAliveInterval);

        if (options.has("cleanSession")) {
            cleanSession = options.getBoolean("cleanSession");
        }
        if (options.has("automaticReconnect")) {
            automaticReconnect = options.getBoolean("automaticReconnect");
        }
        if (options.has("userName")) {
            String userName = options.getString("userName");
            connectOptions.setUserName(userName);
        }
        if (options.has("password")) {
            String password = options.getString("password");
            connectOptions.setPassword(password.toCharArray());
        }

        connectOptions.setCleanSession(cleanSession);
        connectOptions.setAutomaticReconnect(automaticReconnect);
        return connectOptions;
    }

    void connect(MqttConnectOptions connectOptions) throws MqttException {
        this.client = new MqttClient(this.broker, this.clientId, this.persistence);
        this.client.connect(connectOptions);
//        this.client.subscribe("connection_test", 1);
    }

    void subscribe(String topic, int qos) throws MqttException {
        client.subscribe(topic, qos);
    }

    void unsubscribe(String topic) throws MqttException {
     client.unsubscribe(topic);
    }

    public void disconnect() throws MqttException {
        client.disconnect();
    }

    MqttClient getClient() {
        return this.client;
    }
}
