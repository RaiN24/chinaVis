package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.domain.Jizhan;
import com.example.domain.Message;

public interface JizhanDao {
	@Insert("insert ignore into t_jizhan(phone,jizhan) values(#{phone,jdbcType=VARCHAR},#{jizhan})")
	public void insert(Jizhan jizhan);
	@Select("select phone from t_jizhan where jizhan=#{jizhan}")
	public List<String> selectPhones(int jizhan);
	@Select("select * from t_message WHERE (SELECT jizhan from t_jizhan WHERE phone=t_message.phone)=#{jizhan}")
	public List<Message> selectMessageByJizhan(int jizhan);
}
