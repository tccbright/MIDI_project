package com.tccb.virtualband.dto;

import lombok.Data;

@Data
public class MqttPayload {
    private String topic;
    private String payload;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
