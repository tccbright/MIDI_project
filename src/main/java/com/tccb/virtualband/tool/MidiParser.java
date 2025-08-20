package com.tccb.virtualband.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.sound.midi.*;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class MidiParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    // tick 转秒
    private static double ticksToSeconds(long ticks, int tpq, double tempo) {
        return (ticks * (tempo / 1_000_000.0)) / tpq;
    }

    // MIDI → JSON
    public static ArrayNode parseMidi(String path) throws Exception {
        Sequence seq;

        // 判断是 classpath 还是磁盘文件
        if (path.startsWith("/")) {
            try (InputStream is = MidiParser.class.getResourceAsStream(path)) {
                if (is == null) {
                    throw new IllegalArgumentException("资源未找到: " + path);
                }
                seq = MidiSystem.getSequence(is);
            }
        } else {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("文件未找到: " + file.getAbsolutePath());
            }
            seq = MidiSystem.getSequence(file);
        }

        return parseSequence(seq);
    }

    // 公共解析逻辑
    private static ArrayNode parseSequence(Sequence seq) throws Exception {
        int ticksPerBeat = seq.getResolution();
        double tempo = 500000; // 初始tempo

        int[] channelProgram = new int[16];
        Arrays.fill(channelProgram, 0);

        Map<Integer, List<ObjectNode>> programEvents = new HashMap<>();

        for (Track track : seq.getTracks()) {
            long absTicks = 0;
            for (int i = 0; i < track.size(); i++) {
                MidiEvent evt = track.get(i);
                absTicks = evt.getTick();
                double sec = ticksToSeconds(absTicks, ticksPerBeat, tempo);

                MidiMessage msg = evt.getMessage();
                if (msg instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) msg;
                    int ch = sm.getChannel();

                    if (sm.getCommand() == ShortMessage.PROGRAM_CHANGE) {
                        channelProgram[ch] = sm.getData1();
                        ObjectNode e = mapper.createObjectNode();
                        e.put("time", sec);
                        e.put("type", "program");
                        e.put("ch", ch);
                        e.put("program", sm.getData1());
                        programEvents.computeIfAbsent(sm.getData1(), k -> new ArrayList<>()).add(e);

                    } else if (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() > 0) {
                        int prog = channelProgram[ch];
                        ObjectNode e = mapper.createObjectNode();
                        e.put("time", sec);
                        e.put("type", "noteOn");
                        e.put("ch", ch);
                        e.put("pitch", sm.getData1());
                        e.put("vel", sm.getData2());
                        programEvents.computeIfAbsent(prog, k -> new ArrayList<>()).add(e);

                    } else if (sm.getCommand() == ShortMessage.NOTE_OFF ||
                            (sm.getCommand() == ShortMessage.NOTE_ON && sm.getData2() == 0)) {
                        int prog = channelProgram[ch];
                        ObjectNode e = mapper.createObjectNode();
                        e.put("time", sec);
                        e.put("type", "noteOff");
                        e.put("ch", ch);
                        e.put("pitch", sm.getData1());
                        programEvents.computeIfAbsent(prog, k -> new ArrayList<>()).add(e);
                    }
                } else if (msg instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage) msg;
                    if (mm.getType() == 0x51) { // set_tempo
                        byte[] data = mm.getData();
                        tempo = ((data[0] & 0xFF) << 16) |
                                ((data[1] & 0xFF) << 8) |
                                (data[2] & 0xFF);
                    }
                }
            }
        }

        ArrayNode root = mapper.createArrayNode();
        for (Map.Entry<Integer, List<ObjectNode>> entry : programEvents.entrySet()) {
            entry.getValue().sort(Comparator.comparingDouble(e -> e.get("time").asDouble()));
            ObjectNode obj = mapper.createObjectNode();
            obj.put("program", entry.getKey());
            ArrayNode arr = mapper.createArrayNode();
            entry.getValue().forEach(arr::add);
            obj.set("events", arr);
            root.add(obj);
        }

        return root;
    }
}
