package com.tccb.virtualband.tool;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonSaver {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String saveJson(ArrayNode instruments, String midiPath) throws Exception {
        // 提取文件名部分
        String baseName = new File(midiPath).getName();
        if (baseName.toLowerCase().endsWith(".mid")) {
            baseName = baseName.substring(0, baseName.length() - 4);
        }

        // 生成时间戳
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = baseName + "_" + timestamp + ".json";

        // 存到 data/MusicJson 目录
        File dir = new File("data/MusicJson");
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("❌ 创建目录失败: " + dir.getAbsolutePath());
        }

        File outFile = new File(dir, fileName);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, instruments);

        // ✅ 返回相对路径
        return "data/MusicJson/" + fileName;
    }


}

