package com.tccb.virtualband.service;

import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.tool.Result;
import org.springframework.web.multipart.MultipartFile;

public interface MidiService {
    Result<Song> saveMidi(MultipartFile file, MidiUploadDto midiInfo);           // 只保存 JSON

    Result<Song> saveAndPublishMidi(MultipartFile file, MidiUploadDto midiInfo); // 保存并推送 MQTT  // 保存并推送 MQTT
}
