package com.tccb.virtualband.tool;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import org.eclipse.paho.client.mqttv3.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class MidiMqttPublisher {
    private static final ObjectMapper mapper = new ObjectMapper();

    // 🎵 JSON → MQTT 推送
    public static void publishJsonOverMqtt(String jsonPath, String broker, int chunkSize) throws Exception {
        MqttClient client = new MqttClient("tcp://" + broker + ":1883", MqttClient.generateClientId());
        client.connect();

        byte[] bytes = Files.readAllBytes(new File(jsonPath).toPath());
        JsonNode instruments = mapper.readTree(bytes);

        Map<String, List<JsonNode>> grouped = new HashMap<>();
        for (JsonNode instr : instruments) {
            int prog = instr.get("program").asInt();
            for (JsonNode ev : instr.get("events")) {
                Integer ch = ev.has("ch") ? ev.get("ch").asInt() : null;
                String base = mapProgramToBase(prog, ch);
                grouped.computeIfAbsent(base, k -> new ArrayList<>()).add(ev);
            }
        }

        String[] bases = {"Piano", "Bass", "Violin", "Strings", "Drums"};
        for (String baseName : bases) {
            List<JsonNode> events = grouped.getOrDefault(baseName, Collections.emptyList());
            if (events.isEmpty()) {
                System.out.println("⚠️ " + baseName + " 没有事件，跳过");
                continue;
            }

            List<List<JsonNode>> chunks = new ArrayList<>();
            for (int i = 0; i < events.size(); i += chunkSize) {
                chunks.add(events.subList(i, Math.min(i + chunkSize, events.size())));
            }

            ObjectNode meta = mapper.createObjectNode();
            meta.put("song", jsonPath);
            meta.put("instrument", baseName);
            meta.put("totalChunks", chunks.size());
            client.publish("midi/" + baseName + "/meta",
                    new MqttMessage(mapper.writeValueAsBytes(meta)));

            for (int i = 0; i < chunks.size(); i++) {
                ObjectNode packet = mapper.createObjectNode();
                packet.put("chunk", i + 1);
                ArrayNode arr = mapper.createArrayNode();
                chunks.get(i).forEach(arr::add);
                packet.set("data", arr);

                client.publish("midi/" + baseName + "/chunk",
                        new MqttMessage(mapper.writeValueAsBytes(packet)));
            }
        }

        System.out.println("🎵 全部乐器数据发送完成 ✅");
        client.disconnect();
    }

    private static String mapProgramToBase(int prog, Integer channel) {
        if (channel != null && channel == 9) return "Drums";
        if (prog == 0 || (24 <= prog && prog <= 31)) return "Piano";
        if (32 <= prog && prog <= 39) return "Bass";
        if (40 <= prog && prog <= 47) return "Violin";
        if (48 <= prog && prog <= 55) return "Strings";
        return "Piano";
    }
}
