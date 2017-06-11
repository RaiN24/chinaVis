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
	//已优化
	@Select("SELECT * from t_message join t_jizhan WHERE t_message.phone=t_jizhan.phone AND jizhan=#{jizhan} AND conntime>=#{date,jdbcType=TIMESTAMP} AND conntime<date_add(#{date,jdbcType=TIMESTAMP},INTERVAL 1 day) ORDER BY conntime")
	public List<Message> getMessagesByJizhanAndDate(@Param("jizhan")int jizhan,@Param("date")Timestamp date);
	//已优化
	@Select("SELECT type,FLOOR(hour(conntime)/4) AS time,lng,lat from t_message join t_text WHERE t_message.md5=t_text.md5 AND conntime>=#{date,jdbcType=TIMESTAMP} AND conntime<date_add(#{date,jdbcType=TIMESTAMP},INTERVAL 1 day)")
	public List<TypeTimeLngLat> getTypeTimeAreaByDate(Timestamp date);
	
	@Select("select phone from t_phone_message where count>10")
	public List<String> getAllPhones();
	//已优化
	@Select("SELECT type,count(*) as num FROM t_message join t_text WHERE t_message.md5=t_text.md5 AND recitime>=#{date,jdbcType=TIMESTAMP} AND recitime<date_add(#{date,jdbcType=TIMESTAMP},INTERVAL 1 day) GROUP BY type")
	public List<TypeNum> getTypeMessageByDate(Timestamp date);
	
	@Select("Select lng,lat from t_message")
	public List<Pos> selectAllMessage();
}
