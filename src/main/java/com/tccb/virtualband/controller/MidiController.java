package com.tccb.virtualband.controller;

import com.tccb.virtualband.dto.MidiFileDto;
import com.tccb.virtualband.dto.MidiUploadDto;
import com.tccb.virtualband.entity.Song;
import com.tccb.virtualband.service.MidiService;
import com.tccb.virtualband.tool.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    /**
     * 返回名字和id
     * @return
     */
    @GetMapping("/list")
    public List<MidiFileDto> listMidiFiles() {
        return midiService.getAllMidiFiles();
    }


    /**
     * 查具体的歌
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Song getMidiDetail(@PathVariable Long id) {
        return midiService.getMidiDetail(id);
    }


    /**
     * 推送歌曲
     * @param id
     * @return
     */
    @PostMapping("/push/{id}")
    public Result<String> pushMidi(@PathVariable Long id) {
        boolean success = midiService.pushMidiToMqtt(id);
        if (success) {
            return Result.ok("publish success!");
        } else {
            return Result.fail("publish fail!");
        }
    }

    /**
     * 播放歌曲
     * @return
     */
    @PostMapping("/play")
    public Result<String> play() {
        midiService.sendControlCommand("{\"cmd\":\"start\",\"delayMs\":1000}");
        return Result.ok("The playback command has been sent");
    }

    /**
     * 暂停歌曲
     * @return
     */
    @PostMapping("/pause")
    public Result<String> pause() {
        midiService.sendControlCommand("{\"cmd\":\"stop\"}");
        return Result.ok("The stop command has been sent");
    }
}
