package com.tccb.virtualband.controller;

import com.tccb.virtualband.dto.MqttPayload;
import com.tccb.virtualband.service.MqttService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mqtt")
public class MqttController {

    @Autowired
    private MqttService mqttService;

    @PostMapping("/publish")
    public String publish(@RequestBody MqttPayload payload) {
        return mqttService.publish(payload);
    }
}