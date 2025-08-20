package com.tccb.virtualband.mapper;

import com.tccb.virtualband.entity.Song;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SongMapper {

    @Insert("INSERT INTO song(name, description, json_url, instrument_count, created_at) " +
            "VALUES(#{name}, #{description}, #{jsonUrl}, #{instrumentCount}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSong(Song song);

    @Select("SELECT * FROM song ORDER BY created_at DESC")
    List<Song> findAll();

    @Select("SELECT * FROM song WHERE id = #{id}")
    Song findById(Long id);

    @Delete("DELETE FROM song WHERE id = #{id}")
    int deleteById(Long id);
}
