package com.example.domain;

import java.util.Date;

public class MyDate extends Date {
	public MyDate(String substring) {
		super(substring);
	}
	public MyDate() {
		super();
	}
	@Override
	public int hashCode() {
		return this.getYear()%7+this.getMonth()%11+this.getDate()%13;
	}
	@Override
	public boolean equals(Object obj) {
		MyDate date=(MyDate)obj;
		return this.getYear()==date.getYear()&&this.getMonth()==date.getMonth()&&this.getDate()==date.getDate();
	}
}
