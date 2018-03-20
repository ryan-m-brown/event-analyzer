package com.masd.event.db;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.Set;

/**
 *
 */
public interface ExecutionMapper {

    @Insert("INSERT INTO execution(file_hash,exec_time,score,label) VALUES(#{file_hash},#{exec_time},#{score},#{label})")
    void insertExecution(@Param("file_hash") Long fileHash, @Param("exec_time") Date execTimestamp,
                     @Param("score") Float score, @Param("label") String label);


    @Results({
            @Result(property = "label", column = "label"),
    })
    @Select("SELECT label FROM execution WHERE file_hash=#{fileHash}")
    Set<String> selectPreviousExecution(@Param("fileHash") Long fileHash);
}
