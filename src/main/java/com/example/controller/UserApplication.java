package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.domain.User;
import com.example.service.UserService;


@RestController
public class UserApplication{
	@Autowired
	private UserService userService;
	@RequestMapping(value="/users")
	public List<User> findAll(){
		return userService.findAll();
	}
}
