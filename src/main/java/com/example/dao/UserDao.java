package com.example.dao;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.example.domain.User;

public interface UserDao {
	@Select("select * from user")
	public List<User> findAll();
}
