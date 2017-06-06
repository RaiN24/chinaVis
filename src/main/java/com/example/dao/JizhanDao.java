package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.domain.Jizhan;

public interface JizhanDao {
	@Insert("insert ignore into t_jizhan(phone,jizhan) values(#{phone,jdbcType=VARCHAR},#{jizhan})")
	public void insert(Jizhan jizhan);
	@Select("select phone from t_jizhan where jizhan=#{jizhan}")
	public List<String> selectPhones(int jizhan);
}
