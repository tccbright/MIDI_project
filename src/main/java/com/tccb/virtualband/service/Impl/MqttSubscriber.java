package com.tccb.virtualband.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tccb.virtualband.controller.StatusWebSocketHandler;
import com.tccb.virtualband.entity.InstrumentStatus;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MqttSubscriber {

    private final StatusWebSocketHandler wsHandler;
    private static final ObjectMapper mapper = new ObjectMapper();
    private MqttClient client; // ← 持有为成员


    public MqttSubscriber(StatusWebSocketHandler wsHandler) {
        this.wsHandler = wsHandler;
    }

    @PostConstruct
    public void init() {
        try {
            client = new MqttClient("tcp://172.20.10.10:1883", "spring-mqtt-subscriber", null);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);           // ← 持久会话，避免重连后订阅丢失
            options.setKeepAliveInterval(500);

            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    try {
                        // 重连后确保订阅仍然有效（即使 cleanSession=false，也二次保障）
                        client.subscribe("midi/status", 0, (topic, message) -> {
                            String payload = new String(message.getPayload());
                            System.out.println("收到 MQTT 状态: " + payload);
                            InstrumentStatus status = mapper.readValue(payload, InstrumentStatus.class);
                            wsHandler.broadcast(status);
                        });
                        System.out.println("已（重）订阅 midi/status");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    System.err.println("MQTT 连接丢失: " + cause);
                }
                @Override
                public void messageArrived(String topic, MqttMessage message) { /* 用上面的 per-topic 监听 */ }
                @Override
                public void deliveryComplete(IMqttDeliveryToken token) { }
            });
            client.connect(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


