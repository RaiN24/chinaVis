package com.example.domain;

import java.sql.Timestamp;

public class Send {
	Timestamp time;
	Integer type;
	public Timestamp getTime() {
		return time;
	}
	public void setTime(Timestamp time) {
		this.time = time;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
}
