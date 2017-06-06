package com.example.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.domain.Text;

public interface TextDao {
	@Insert("insert ignore into t_text(md5,content) values(#{md5},#{content})")
	public void insertText(Text text);
	@Select("select type from t_text where t_text.md5=#{md5}")
	public int getType(String md5);
}
