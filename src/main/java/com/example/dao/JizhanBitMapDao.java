package com.example.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface JizhanBitMapDao {
	@Insert("insert ignore into t_jizhan_bitmap(jizhan,bit_map) values(#{jizhan},#{bitMap})")
	public void insertText(@Param("jizhan")int jizhan,@Param("bitMap")String bitMap);

	@Select("select bit_map from t_jizhan_bitmap where jizhan=#{jizhan} ")
	public String getText(String jizhan);
}
