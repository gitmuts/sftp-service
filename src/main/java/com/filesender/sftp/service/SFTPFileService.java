package com.filesender.sftp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.filesender.sftp.config.SFTPConfiguration;
import com.filesender.sftp.model.ProcessResponse;
import com.filesender.sftp.model.ServerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SFTPFileService {

	Logger logger = LoggerFactory.getLogger(SFTPFileService.class);

	List<ServerInfo> servers = SFTPConfiguration.servers;

	@Autowired
	AsyncService asyncService;

	public List<String> sendFiles() {
		
		List<String> failures = new ArrayList<>();
		
		try {

			Long startTime = System.currentTimeMillis();

			List<CompletableFuture<ProcessResponse>> responses = new ArrayList<>();

			for (ServerInfo server : servers) {
				CompletableFuture<ProcessResponse>  response = asyncService.zipAndSendFile(server);
				responses.add(response);
			}

			
			CompletableFuture.allOf(responses.toArray(new CompletableFuture[responses.size()])).join();
			
			for(CompletableFuture<ProcessResponse> receivedResponse : responses) {
				
				if(!receivedResponse.get().isSent()) {
					failures.add(receivedResponse.get().getServerName());
				}
			}
			
			long endTime = System.currentTimeMillis();

			logger.info(String.format("SFTP process has completed in %s  seconds" , (endTime- startTime)/ 1000));

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return failures;
	}
}
