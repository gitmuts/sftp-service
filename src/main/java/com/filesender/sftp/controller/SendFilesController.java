package com.filesender.sftp.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.filesender.sftp.config.SFTPConfiguration;
import com.filesender.sftp.model.FileUploadRecord;
import com.filesender.sftp.model.ServerInfo;
import com.filesender.sftp.service.ArchivalService;
import com.filesender.sftp.service.DatabaseService;
import com.filesender.sftp.service.SFTPFileService;

@RestController
public class SendFilesController {

	Logger logger = LoggerFactory.getLogger(SendFilesController.class);

	@Autowired
	SFTPFileService sftpService;

	List<ServerInfo> servers = SFTPConfiguration.servers;
	
	@Autowired
	DatabaseService databaseService;

	@Autowired
	ArchivalService archivalService;
	
	@RequestMapping(value = "/sendfiles", method = RequestMethod.GET)
	public ResponseEntity<?> sendFiles() {
		try {
			
			long startTime = System.currentTimeMillis();
			
			String responseMessage ="";
			List<String> failures = sftpService.sendFiles(servers);
			
			if(failures.size() > 0) {
				responseMessage= "Failed to connect to the servers: " + failures.toString();
				logger.warn("#### "+ responseMessage  +" ###");
			}else {
				responseMessage = "Files sent successfully to all servers";
			}
		
			long endTime = System.currentTimeMillis();
			
			long seconds = (endTime - startTime) /1000;
			
			logger.info("Process took {} seconds" , seconds);
			
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		
	}
	
	@RequestMapping(value = "/getrecords", method = RequestMethod.GET)
	public ResponseEntity<?> getSentFiles() {
		try {
			
			List<FileUploadRecord> records = databaseService.getSentFiles();
			
			return new ResponseEntity<>(records, HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/truncatetable", method = RequestMethod.GET)
	public ResponseEntity<?> truncateFileUpload() {
		try {
			
			List<FileUploadRecord> records = databaseService.getSentFiles();
			
			archivalService.archiveFileUploadRecords(records);

			return new ResponseEntity<>("Files archived", HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/resendfile", method = RequestMethod.POST)
	public ResponseEntity<?> resendFile(@RequestBody FileUploadRecord record) {
		try {
			
			ServerInfo serverInfo = null;
			
			for(ServerInfo server : servers) {
				if(server.getName().equals(record.getBranch())) {
					serverInfo = server;
				}
			}
			
			if(serverInfo == null) {
				return new ResponseEntity<>( "Server config not found", HttpStatus.BAD_REQUEST);
			}
			
			List<ServerInfo> resendTo = new ArrayList<>();
			resendTo.add(serverInfo);
			
			List<String> failures = sftpService.sendFiles(resendTo);
			
			String responseMessage;
			HttpStatus status;
			
			if(failures.size() > 0) {
				responseMessage= "Failed to connect to the servers: " + failures.toString();
				logger.warn("#### "+ responseMessage  +" ###");
				status = HttpStatus.INTERNAL_SERVER_ERROR;
				
			}else {
				responseMessage = "File sent successfully to "+ record.getBranch();
				status = HttpStatus.OK;
			}
			
			
			return new ResponseEntity<>(responseMessage, status);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
