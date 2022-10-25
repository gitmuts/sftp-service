package com.filesender.bpowersftp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.filesender.bpowersftp.model.ListResponse;
import com.filesender.bpowersftp.repo.FileUploadRecordRepo;
import com.filesender.bpowersftp.service.GetFilesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.filesender.bpowersftp.config.SFTPConfiguration;
import com.filesender.bpowersftp.model.FileUploadRecord;
import com.filesender.bpowersftp.model.ServerInfo;
import com.filesender.bpowersftp.service.ArchivalService;
import com.filesender.bpowersftp.service.CommonFileUtil;
import com.filesender.bpowersftp.service.DatabaseService;
import com.filesender.bpowersftp.service.SFTPFileService;

@RestController
@RequestMapping("/api")
public class SendFilesController {

	Logger logger = LoggerFactory.getLogger(SendFilesController.class);

	@Autowired
	SFTPFileService sftpService;

	List<ServerInfo> servers = SFTPConfiguration.servers;
	
	@Autowired
	DatabaseService databaseService;

	@Autowired
	ArchivalService archivalService;

	@Autowired
	GetFilesService getFilesService;

	@Autowired
	CommonFileUtil commonFileUtil;

	@Autowired
	FileUploadRecordRepo fileUploadRecordRepo;

	@RequestMapping(value = "/sendfiles", method = RequestMethod.GET)
	public ResponseEntity<?> sendFiles() {
		try {

			logger.info("Received a request to send file to all branches");

//			if(processRunning){
//				logger.info("Another process is running, please wait");
//				return new ResponseEntity<>("Another process is running, please wait", HttpStatus.BAD_REQUEST);
//			}


			long startTime = System.currentTimeMillis();


			//		processRunning = true;

			String responseMessage ="";

			boolean getFiles = getFilesService.getFiles(servers);
			
			if(!getFiles){
				return new ResponseEntity<>("Operation failed, check logs", HttpStatus.OK);
			}

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

		//	processRunning = false;
			return new ResponseEntity<>(responseMessage, HttpStatus.OK);
			
		}catch(Exception e) {
			//	processRunning = false;
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@RequestMapping(value = "/getrecords", method = RequestMethod.GET)
	public ResponseEntity<?> getSentFiles(@RequestParam("per_page") int perPage,@RequestParam("page") int page) {
		try {

			page = page -1;
			Pageable pageable = PageRequest.of(page, perPage);

			Page<FileUploadRecord> fileUploadRecordPage = fileUploadRecordRepo.findAllByOrderByIdDesc(pageable);

			return new ResponseEntity(new ListResponse(fileUploadRecordPage.getContent(), fileUploadRecordPage.getTotalPages(), fileUploadRecordPage.getNumberOfElements()), HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/truncatetable", method = RequestMethod.GET)
	public ResponseEntity<?> truncateFileUpload() {
		try {
			
			List<FileUploadRecord> records = fileUploadRecordRepo.findAll();
			
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

	@RequestMapping(value = "/getbranches", method = RequestMethod.GET)
	public ResponseEntity<?> getBranches() {
		try {
	
			return new ResponseEntity<>(servers, HttpStatus.OK);
			
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@RequestMapping(value = "/sendtobranch", method = RequestMethod.GET)
	public ResponseEntity<?> sendToSpecificBranch(@RequestParam("code") String branchCode) {
		try {
			
			long startTime = System.currentTimeMillis();

			List<ServerInfo> server = servers.stream().filter(s -> s.getCode().equals(branchCode)).collect(Collectors.toList());

			String responseMessage ="";


			getFilesService.getFiles(server);

			List<String> failures = sftpService.sendFiles(server);
			
			if(failures.size() > 0) {
				responseMessage= "Failed to connect to the servers: " + failures.toString();
				logger.warn("#### "+ responseMessage  +" ###");
			}else {
				responseMessage = "Files sent successfully to server with code "+ branchCode;
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
}
