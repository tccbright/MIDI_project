package com.tccb.virtualband.dto;

import java.time.LocalDateTime;

public class MidiUploadDto {
    private String name;            // 歌曲名称
    private String description;     // 简介

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
