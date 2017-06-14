package com.example.dao;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.domain.Text;

public interface TextDao {
	@Insert("insert ignore into t_text(md5,content) values(#{md5},#{content})")
	public void insertText(Text text);
	@Select("select * from t_text")
	public List<Text> selectAll();
	@Select("select type from t_text where t_text.md5=#{md5}")
	public int getType(String md5);
	@Update("update t_text set type=#{type} where md5=#{md5}")
	public void updateType(@Param("md5")String md5,@Param("type")int type);
}
