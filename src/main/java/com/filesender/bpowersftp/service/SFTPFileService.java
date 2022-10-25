package com.filesender.bpowersftp.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.filesender.bpowersftp.model.FileUploadRecord;
import com.filesender.bpowersftp.model.ProcessResponse;
import com.filesender.bpowersftp.model.ServerInfo;

@Service
public class SFTPFileService {

	Logger logger = LoggerFactory.getLogger(SFTPFileService.class);

	@Autowired
	AsyncService asyncService;

	@Autowired
	DatabaseService databaseService;
	
	@Value("${sftp_common_folder}")
	String commonDir;

	public List<String> sendFiles(List<ServerInfo> servers) {
		
		List<String> failures = new ArrayList<>();
		
		try {
			
			List<CompletableFuture<ProcessResponse>> responses = new ArrayList<>();

			Long startTime = System.currentTimeMillis();

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
			

			//delete all files in common dir
			deleteAllFilesCommon();
			
			long endTime = System.currentTimeMillis();

			long minutes = (endTime - startTime) / 60000;

			logger.info("FTP process has completed in {} seconds /  {} minutes ", (endTime - startTime)/ 1000,  minutes);
			
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return failures;
	}

	private void deleteAllFilesCommon() {
		try{
			File folder = new File(commonDir);

			if (folder.isDirectory()) {
				File[] files = folder.listFiles();
				for (File file : files) {
					 file.delete();
					}
				}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
	}
}
