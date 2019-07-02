package com.filesender.sftp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.filesender.sftp.config.SFTPConfiguration;
import com.filesender.sftp.model.FileUploadRecord;
import com.filesender.sftp.model.ProcessResponse;
import com.filesender.sftp.model.ServerInfo;

@Service
public class SFTPFileService {

	Logger logger = LoggerFactory.getLogger(ZipFilesService.class);

	

	@Autowired
	AsyncService asyncService;
	
	@Autowired
	DatabaseService databaseService;

	public List<String> sendFiles(List<ServerInfo> servers) {
		
		List<String> failures = new ArrayList<>();
		
		try {
			
			List<CompletableFuture<ProcessResponse>> responses = new ArrayList<>();

			for (ServerInfo server : servers) {
				FileUploadRecord record = new FileUploadRecord();
				record.setBranch(server.getName());
				record.setStatus(FileUploadRecord.Status.IN_PROGRESS);
				
				long recordId = databaseService.createFileUpoadRecord(record);
				
				server.setRecordId(recordId);
				
				CompletableFuture<ProcessResponse>  response = asyncService.zipAndSendFile(server);
				responses.add(response);
			}

			
			CompletableFuture.allOf(responses.toArray(new CompletableFuture[responses.size()])).join();
			
			for(CompletableFuture<ProcessResponse> receivedResponse : responses) {
				
				FileUploadRecord record = new FileUploadRecord();
				
				if(!receivedResponse.get().isSent()) {
					record.setDesc("Failed to send file");
					record.setStatus(FileUploadRecord.Status.FAILED);
					failures.add(receivedResponse.get().getServerName());
				} else {
					record.setStatus(FileUploadRecord.Status.SUCCESS);
					record.setDesc("Sent successfully");
				}
				
				databaseService.updateFileUploadRecord(receivedResponse.get().getRecordId(), record);
				
			}
			
			logger.info("FTP process has completed");
			
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return failures;
	}

	
	
}
