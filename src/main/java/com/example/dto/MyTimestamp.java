package com.example.dto;

import java.sql.Timestamp;

public class MyTimestamp extends Timestamp {
	
	public MyTimestamp(long time) {
		super(time);
	}
	@Override
	public int hashCode() {
		return this.getYear()*10000+this.getMonth()*100+this.getDate();
	}
	@Override
	public boolean equals(Object o) {
		MyTimestamp ts=(MyTimestamp)o;
		return this.getYear()*10000+this.getMonth()*100+this.getDate()==ts.getYear()*10000+ts.getMonth()*100+ts.getDate();
	}
	@Override
	public String toString() {
		return this.getYear()+"-"+this.getMonth()+"-"+this.getDate();
	}
}
