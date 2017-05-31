package com.example.dao;

import org.apache.ibatis.annotations.Insert;

import com.example.domain.Text;

public interface TextDao {
	@Insert("insert ignore into t_text(md5,content) values(#{md5},#{content})")
	public void insertText(Text text);
}
