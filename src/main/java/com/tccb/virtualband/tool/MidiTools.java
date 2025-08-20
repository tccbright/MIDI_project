package com.tccb.virtualband.tool;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tccb.virtualband.tool.*;

public class MidiTools {
    /**
     * 将midi转成json直接发部出去,返回url
     * @param resourcePath
     * @param broker
     * @throws Exception
     */
    public static String processMidi(String resourcePath, String broker) throws Exception {
        // 1. 从 classpath 解析 MIDI → JSON
        ArrayNode instruments = MidiParser.parseMidi(resourcePath);
        // 2. 自动生成 JSON 文件名 (midi名+时间戳)
        String jsonPath = JsonSaver.saveJson(instruments, resourcePath);
        // 3. 推送 MQTT
        MidiMqttPublisher.publishJsonOverMqtt(jsonPath, broker, 100);
        return jsonPath;
    }

    /**
     * 将midi转成json保存到data/json,返回url
     * @param resourcePath
     * @throws Exception
     */
    public static String saveMidi(String resourcePath) throws Exception {
        // 1. 从 classpath 解析 MIDI → JSON
        ArrayNode instruments = MidiParser.parseMidi(resourcePath);

        // 2. 自动生成 JSON 文件名 (midi名+时间戳)
        return JsonSaver.saveJson(instruments, resourcePath);
    }

    /**
     * 直接读取已有 JSON 文件并推送到 MQTT
     */
    public static void replayJson(String jsonPath, String broker) throws Exception {
        MidiMqttPublisher.publishJsonOverMqtt(jsonPath, broker, 100);
    }


}
