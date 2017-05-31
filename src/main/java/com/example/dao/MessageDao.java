package com.example.dao;

import java.sql.Date;

import org.apache.ibatis.annotations.Insert;

import com.example.domain.Message;

public interface MessageDao {
	@Insert("insert into t_message(md5,phone,conntime,recitime,lng,lat) values(#{md5},#{phone},#{conntime},#{recitime},#{lng},#{lat})")
	public void insertMessage(Message message);
}
