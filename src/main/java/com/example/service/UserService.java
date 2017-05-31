package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dao.UserDao;
import com.example.domain.User;

@Service
public class UserService {
	@Autowired
	private UserDao userDao;
	
	@Transactional
	public List<User> findAll(){
		return userDao.findAll(); 
	}
}
