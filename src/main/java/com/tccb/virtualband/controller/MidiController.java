package com.tccb.virtualband.controller;

import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.service.MidiService;
import com.tccb.virtualband.tool.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/midi")
@Slf4j
public class MidiController {

    /**
     * 注入
     */
    private final MidiService midiService;
    public MidiController(MidiService midiService) {
        this.midiService = midiService;
    }

    /**
     * 只保存 JSON
     * @param file
     * @return
     */
    @PostMapping("/save")
    public Result<Song> saveMidi(@RequestParam("file") MultipartFile file,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description) {
        MidiUploadDto dto = new MidiUploadDto();
        dto.setName(name);
        dto.setDescription(description);
        return midiService.saveMidi(file, dto);
    }


    /**
     * 保存json并推送 MQTT
     * @param file
     * @return
     */
    @PostMapping("/savePublish")
    public Result<Song> saveAndPublishMidi(@RequestParam("file") MultipartFile file,
                                     @RequestParam("name") String name,
                                     @RequestParam(value = "description", required = false) String description) {
        MidiUploadDto dto = new MidiUploadDto();
        dto.setName(name);
        dto.setDescription(description);
        return midiService.saveAndPublishMidi(file, dto);
    }
}
