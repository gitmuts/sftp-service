package com.filesender.sftp.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.stereotype.Service;

import com.filesender.sftp.config.SFTPConfiguration;
import com.filesender.sftp.config.SFTPConfiguration.UploadGateway;
import com.filesender.sftp.model.ServerInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

@Service
public class SFTPFileService {

	Logger logger = LoggerFactory.getLogger(ZipFilesService.class);

	List<ServerInfo> servers = SFTPConfiguration.servers;

	@Autowired
	ZipFilesService zipService;

	@Autowired
	private UploadGateway gateway;

	@Autowired
	DelegatingSessionFactory<LsEntry> sftpSessionFactory;

	public List<String> sendFiles() {
		
		List<String> failures = new ArrayList<>();
		
		try {

			for (ServerInfo server : servers) {
				// zip files
				String zippedFilePath = zipService.zipFilesInDirectory(server.getFolderName(), server.getCode());

				// send through sftp
				logger.info(String.format("Sending file to %s", server.getName()));
				boolean sent = sendFile(zippedFilePath, server.getName());
				if (sent) {
					logger.info(String.format("File sent successfully to %s", server.getName()));
				} else {
					logger.warn(String.format("Failed to send file to %s", server.getName()));
					failures.add(server.getName());
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		return failures;
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
