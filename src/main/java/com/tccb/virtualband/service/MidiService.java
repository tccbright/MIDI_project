package com.tccb.virtualband.service;

import com.tccb.virtualband.dto.MidiFileDto;
import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.tool.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MidiService {
    Result<Song> saveMidi(MultipartFile file, MidiUploadDto midiInfo);           // 只保存 JSON

    Result<Song> saveAndPublishMidi(MultipartFile file, MidiUploadDto midiInfo); // 保存并推送 MQTT  // 保存并推送 MQTT

    /**
     * 找到数据库所有的midi
     * @return
     */
    List<MidiFileDto> getAllMidiFiles();

    /**
     * 查具体的歌
     * @param id
     * @return
     */
    Song getMidiDetail(Long id);

    /**
     * 推送歌曲到mqtt
     * @param id
     * @return
     */
    boolean pushMidiToMqtt(Long id);

    /**
     * 播放暂停
     * @param s
     */
    void sendControlCommand(String s);
}
