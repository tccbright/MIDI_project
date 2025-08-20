package com.tccb.virtualband.entity;

import java.time.LocalDateTime;

public class Song {
    private Long id;                // 主键
    private String name;            // 歌曲名称
    private String description;     // 简介
    private String jsonUrl;         // JSON 路径
    private Integer instrumentCount;// 乐器数量
    private LocalDateTime createdAt;// 创建时间


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getJsonUrl() { return jsonUrl; }
    public void setJsonUrl(String jsonUrl) { this.jsonUrl = jsonUrl; }

    public Integer getInstrumentCount() { return instrumentCount; }
    public void setInstrumentCount(Integer instrumentCount) { this.instrumentCount = instrumentCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
