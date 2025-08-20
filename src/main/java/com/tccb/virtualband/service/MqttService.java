package com.tccb.virtualband.service;

import com.tccb.virtualband.dto.MqttPayload;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MqttService {
    private MqttClient client;

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient("tcp://172.20.10.10:1883", MqttClient.generateClientId(), new MemoryPersistence());
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String publish(MqttPayload payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getPayload().getBytes());
            message.setQos(0);
            client.publish(payload.getTopic(), message);
            return "Message sent to topic: " + payload.getTopic();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send message.";
        }
    }
}