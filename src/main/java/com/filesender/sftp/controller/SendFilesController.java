package com.filesender.sftp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.filesender.sftp.service.SFTPFileService;

@RestController
public class SendFilesController {

	Logger logger = LoggerFactory.getLogger(SendFilesController.class);

	@Autowired
	SFTPFileService sftpService;

	
	@RequestMapping(value = "/sendfiles", method = RequestMethod.GET)
	public String sendFiles() {
		try {
			
			List<String> failures = sftpService.sendFiles();
			
			if(failures.size() > 0) {
				logger.warn("#### Failed to connect to the servers: " + failures.toString());
			}
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return "sent";
	}
}
