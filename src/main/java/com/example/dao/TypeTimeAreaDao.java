package com.example.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.example.domain.TypeTimeArea;

public interface TypeTimeAreaDao {
	@Insert("insert ignore into t_type_time_area(date,text) values(#{date},#{text})")
	public void insertText(@Param("date")String date,@Param("text")String text);

	@Select("select text from t_type_time_area where date=#{date,jdbcType=VARCHAR} ")
	public String getText(String date);
}
