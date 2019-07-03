package com.filesender.sftp.service;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.filesender.sftp.config.SFTPConfiguration.UploadGateway;
import com.filesender.sftp.model.ProcessResponse;
import com.filesender.sftp.model.ServerInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * AsyncService
 */
@Service
public class AsyncService {

    Logger logger = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
	ZipFilesService zipService;

	@Autowired
	private UploadGateway gateway;
	
	@Autowired
	DelegatingSessionFactory<LsEntry> sftpSessionFactory;
	
	@Async
	public CompletableFuture<ProcessResponse> zipAndSendFile(ServerInfo server) {
		// zip files
		String zippedFilePath = zipService.zipFilesInDirectory(server.getFolderName(), server.getCode());

		// send through sftp
		logger.info(String.format("Sending file to %s", server.getName()));
		
		boolean sent = sendFile(zippedFilePath, server.getName());
		
		if (sent) {
			logger.info(String.format("File sent successfully to %s", server.getName()));
		} else {
			logger.warn(String.format("Failed to send file to %s", server.getName()));
		}
		
		return CompletableFuture.completedFuture(new ProcessResponse(server.getName(), sent));
	}

	public boolean sendFile(String filePath, String serverName) {
		try {
			sftpSessionFactory.setThreadKey(serverName);
			File file = new File(filePath);
			if (file.exists()) {
				gateway.upload(file);
				return true;
			} else {
				logger.warn(String.format("%s does not exist", filePath));
				return false;
			}
		} catch (Exception e) {
			logger.error(String.format("Error occurred while sending file for %s, detailed error is %s", serverName,
					e.getMessage()));
			return false;
		}
	}
}