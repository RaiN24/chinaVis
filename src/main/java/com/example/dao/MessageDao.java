package com.example.dao;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.domain.Message;

public interface MessageDao {
	@Insert("insert into t_message(md5,phone,conntime,recitime,lng,lat) values(#{md5},#{phone},#{conntime},#{recitime},#{lng},#{lat})")
	public void insertMessage(Message message);
	@Select("select * from t_message where phone=#{phone,jdbcType=VARCHAR}")
	public List<Message> getMessagesByPhone(String phone);
	@Select("select phone from t_message group by phone order by count(phone) DESC LIMIT 30")
	public List<String> getAllPhones();
}
