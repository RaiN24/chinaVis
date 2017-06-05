package com.example.dao;

import org.apache.ibatis.annotations.Insert;

import com.example.domain.Jizhan;

public interface JizhanDao {
	@Insert("insert ignore into t_jizhan(phone,jizhan) values(#{phone,jdbcType=VARCHAR},#{jizhan})")
	public void insert(Jizhan jizhan);
}
