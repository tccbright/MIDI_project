package com.tccb.virtualband.service.Impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tccb.virtualband.dto.MidiFileDto;
import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.mapper.SongMapper;
import com.tccb.virtualband.service.MidiService;
import com.tccb.virtualband.tool.MidiParser;
import com.tccb.virtualband.tool.MidiTools;
import com.tccb.virtualband.tool.Result;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service

public class MidiServiceImpl implements MidiService {

    @Autowired
    private SongMapper songMapper;
    private static final Logger log = LoggerFactory.getLogger(MidiServiceImpl.class);
    private final String broker = "172.20.10.10"; // MQTT broker 地址
    private final String topic = "midi/control";

    @Override
    public Result<Song> saveMidi(MultipartFile file, MidiUploadDto midiInfo) {
        try {
            // 1. 保存到 data/music
            File dir = new File(System.getProperty("user.dir"), "data/music");
            if (!dir.exists()) dir.mkdirs();

            File midiFile = new File(dir, file.getOriginalFilename());
            file.transferTo(midiFile);

            // 2. 调用工具类保存 JSON（不推送）
            String jsonPath = MidiTools.saveMidi(midiFile.getAbsolutePath());

            // 3. 解析 MIDI → 统计乐器数量
            ArrayNode instruments = MidiParser.parseMidi(midiFile.getAbsolutePath());
            Set<Integer> programs = new HashSet<>();
            instruments.forEach(instr -> programs.add(instr.get("program").asInt()));
            int instrumentCount = programs.size();

            // 4. 写入数据库
            Song song = new Song();
            song.setName(midiInfo.getName());
            song.setDescription(midiInfo.getDescription());
            song.setJsonUrl(jsonPath);
            song.setInstrumentCount(instrumentCount);
            songMapper.insertSong(song);

            return Result.ok(song);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("保存失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Song> saveAndPublishMidi(MultipartFile file, MidiUploadDto midiInfo) {
        try {
            // 1. 保存到 data/music
            File dir = new File(System.getProperty("user.dir"), "data/music");
            if (!dir.exists()) dir.mkdirs();

            File midiFile = new File(dir, file.getOriginalFilename());
            file.transferTo(midiFile);

            // 2. 调用工具类保存并推送 MQTT
            String jsonPath = MidiTools.processMidi(midiFile.getAbsolutePath(), broker);

            // 3. 解析 MIDI → 统计乐器数量
            ArrayNode instruments = MidiParser.parseMidi(midiFile.getAbsolutePath());
            Set<Integer> programs = new HashSet<>();
            instruments.forEach(instr -> programs.add(instr.get("program").asInt()));
            int instrumentCount = programs.size();

            // 4. 写入数据库
            Song song = new Song();
            song.setName(midiInfo.getName());
            song.setDescription(midiInfo.getDescription());
            song.setJsonUrl(jsonPath);
            song.setInstrumentCount(instrumentCount);
            songMapper.insertSong(song);

            return Result.ok(song);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("保存推送失败: " + e.getMessage());
        }
    }

    /**
     * 找到数据库所有的midi
     *
     * @return
     */
    @Override
    public List<MidiFileDto> getAllMidiFiles() {
        List<Song> songList = songMapper.findAll();
        List<MidiFileDto> list = new ArrayList<>();

        if (songList != null && !songList.isEmpty()) {
            for (Song song : songList) {
                MidiFileDto dto = new MidiFileDto();
                dto.setId(song.getId());
                dto.setName(song.getName());
                list.add(dto);
            }
        }
        return list;
    }

    /**
     * 查具体的歌
     *
     * @param id
     * @return
     */
    @Override
    public Song getMidiDetail(Long id) {
        Song song=songMapper.findById(id);
        return song;
    }

    /**
     * 推送歌曲到mqtt
     *
     * @param id
     * @return
     */
    @Override
    public boolean pushMidiToMqtt(Long id) {
        Song song = songMapper.findById(id);
        if (song == null || song.getJsonUrl() == null) {
            return false;
        }
        try {
            String jsonPath = song.getJsonUrl();
            log.info("url：{}",jsonPath);
            MidiTools.replayJson(jsonPath, broker);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 播放暂停
     *
     * @param commandJson
     */
    @Override
    public void sendControlCommand(String commandJson) {
        try {
            MqttClient client = new MqttClient("tcp://" + broker + ":1883", MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);

            MqttMessage message = new MqttMessage(commandJson.getBytes());
            message.setQos(1);
            client.publish(topic, message);

            client.disconnect();
            System.out.println("✅ 已发送 MQTT 指令: " + commandJson);
        } catch (Exception e) {
            throw new RuntimeException("发送 MQTT 指令失败", e);
        }
    }
}
