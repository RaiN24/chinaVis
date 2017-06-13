package com.example.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dao.MessageDao;
import com.example.domain.Message;

@Service
public class MessageService {
	@Autowired
	private MessageDao messageDao;
	
	@Transactional
	private List<Message> getMessagesByPhone(String phone){
		return messageDao.getMessagesByPhone(phone);
	}
}
