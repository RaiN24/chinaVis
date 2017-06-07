package com.example.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mockito.internal.verification.Times;

import com.example.domain.Message;
import com.example.domain.Pos;
import com.example.domain.TypeNum;
import com.example.domain.TypeTimeLngLat;

public interface MessageDao {
	@Insert("insert into t_message(md5,phone,conntime,recitime,lng,lat) values(#{md5},#{phone},#{conntime},#{recitime},#{lng},#{lat})")
	public void insertMessage(Message message);
	
	@Select("select * from t_message where phone=#{phone,jdbcType=VARCHAR} and conntime>='2017-02-23 00:00:00' and conntime<'2017-04-27 00:00:00'")
	public List<Message> getMessagesByPhone(String phone);
	
	@Select("select * from t_message where phone=#{phone,jdbcType=VARCHAR} and TO_DAYS(conntime)=TO_DAYS(#{date,jdbcType=TIMESTAMP})")
	public List<Message> getMessagesByPhoneAndDate(@Param("phone")String phone,@Param("date")Timestamp date);
	
	@Select("SELECT * from t_message WHERE phone IN (select phone from t_jizhan where jizhan=#{jizhan}) AND TO_DAYS(conntime)=TO_DAYS(#{date}) ORDER BY conntime")
	public List<Message> getMessagesByJizhanAndDate(@Param("jizhan")int jizhan,@Param("date")Timestamp date);
	
	@Select("SELECT getType(md5) AS type,FLOOR(hour(conntime)/4) AS time,lng,lat from t_message WHERE TO_DAYS(conntime)=TO_DAYS(#{date})")
	public List<TypeTimeLngLat> getTypeTimeAreaByDate(Timestamp date);
	
	@Select("select phone from t_phone_message where count>10")
	public List<String> getAllPhones();
	
	@Select("SELECT getType(md5) as type,count(*) as num FROM t_message WHERE TO_DAYS(recitime)=#{day} GROUP BY type")
	public List<TypeNum> getTypeMessageByDate(int day);
	
	@Select("Select lng,lat from t_message")
	public List<Pos> selectAllMessage();
}
