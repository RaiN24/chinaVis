package com.example.dao;

import java.sql.Timestamp;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.domain.TypeAreaTime;

public interface TypeAreaTimeDao {
	@Insert("insert ignore into t_type_area_time(date,text) values(#{date},#{text})")
	public void insertText(@Param("date")String date,@Param("text")String text);
	
	@Select("select text from t_type_area_time where date=#{date,jdbcType=VARCHAR} ")
	public String getText(String date);
}
