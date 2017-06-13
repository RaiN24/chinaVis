package com.example.domain;

import java.sql.Date;
import java.sql.Timestamp;

public class Message {
	private String md5;
	private String phone;
	private Timestamp conntime;
	private Timestamp recitime;
	private double lng;
	private double lat;
	public Message(){
		
	}
	public Message(String md5,String phone,Timestamp conntime,Timestamp recitime,double lng,double lat) {
		this.md5=md5;
		this.phone=phone;
		this.conntime=conntime;
		this.recitime=recitime;
		this.lng=lng;
		this.lat=lat;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public Timestamp getConntime() {
		return conntime;
	}
	public void setConntime(Timestamp conntime) {
		this.conntime = conntime;
	}
	public Timestamp getRecitime() {
		return recitime;
	}
	public void setRecitime(Timestamp recitime) {
		this.recitime = recitime;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
}
