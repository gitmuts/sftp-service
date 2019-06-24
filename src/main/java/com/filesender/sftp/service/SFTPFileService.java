package com.filesender.sftp.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.filesender.sftp.config.SFTPConfiguration;
import com.filesender.sftp.config.SFTPConfiguration.UploadGateway;
import com.filesender.sftp.model.ProcessResponse;
import com.filesender.sftp.model.ServerInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

@Service
public class SFTPFileService {

	Logger logger = LoggerFactory.getLogger(ZipFilesService.class);

	List<ServerInfo> servers = SFTPConfiguration.servers;

	@Autowired
	AsyncService asyncService;

	public List<String> sendFiles() {
		
		List<String> failures = new ArrayList<>();
		
		try {
			
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
			
			logger.info("FTP process has complted");
			
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return failures;
	}

	
	
}
