package com.tccb.virtualband.service.Impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.mapper.SongMapper;
import com.tccb.virtualband.service.MidiService;
import com.tccb.virtualband.tool.MidiParser;
import com.tccb.virtualband.tool.MidiTools;
import com.tccb.virtualband.tool.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Service
public class MidiServiceImpl implements MidiService {

    @Autowired
    private SongMapper songMapper;

    private final String broker = "172.20.10.10"; // MQTT broker 地址

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
}
