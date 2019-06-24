package com.filesender.sftp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<?> sendFiles() {
		try {
			
			long startTime = System.currentTimeMillis();
			
			String responseMessage ="";
			List<String> failures = sftpService.sendFiles();
			
			if(failures.size() > 0) {
				responseMessage= "Failed to connect to the servers: " + failures.toString();
				logger.warn("#### "+ responseMessage  +" ###");
			}else {
				responseMessage = "Files sent successfully to all servers";
			}
		
			long endTime = System.currentTimeMillis();
			
			long seconds = (endTime - startTime) /1000;
			
			logger.info("Process took " + seconds);
			
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
}
