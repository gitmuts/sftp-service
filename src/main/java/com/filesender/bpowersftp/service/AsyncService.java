package com.filesender.bpowersftp.service;

import java.io.File;
import java.util.concurrent.CompletableFuture;

import com.filesender.bpowersftp.config.SFTPConfiguration.UploadGateway;
import com.filesender.bpowersftp.model.FileUploadRecord;
import com.filesender.bpowersftp.model.ProcessResponse;
import com.filesender.bpowersftp.model.ServerInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncService {

	Logger logger = LoggerFactory.getLogger(AsyncService.class);

	@Autowired
	ZipFilesService zipService;

	@Autowired
	private UploadGateway gateway;

	@Autowired
	DelegatingSessionFactory<LsEntry> sftpSessionFactory;

	@Autowired
	DatabaseService databaseService;

	@Value("${sftp_upload_folder}")
	String destinationFolder;

	@Async("sendExecutor")
	public CompletableFuture<ProcessResponse> zipAndSendFile(ServerInfo server) {
		// zip files
		String zippedFilePath = zipService.zipFilesInDirectory(server.getFolderName(), server.getCode());

		FileUploadRecord record = new FileUploadRecord();

		if (!zippedFilePath.equals("")) {
			
			File zippedFile = new File(zippedFilePath);
			record.setFileSize(zippedFile.length() / 1024/ 1024);

			// send through sftp
			logger.info(String.format("Sending file to %s", server.getName()));

			boolean sent = sendFile(zippedFilePath, server.getName());

			if (sent) {
				record.setStatus(FileUploadRecord.Status.SUCCESS);
				record.setDesc("Sent successfully");
				logger.info(String.format("File sent successfully to %s", server.getName()));
			} else {
				record.setDesc("Failed to send file");
				record.setStatus(FileUploadRecord.Status.FAILED);
				logger.warn(String.format("Failed to send file to %s", server.getName()));
			}

			databaseService.updateFileUploadRecord(server.getRecordId(), record);
			return CompletableFuture.completedFuture(new ProcessResponse(server.getName(), sent, server.getRecordId()));
		} else {
			record.setDesc("File not found for sending");
			record.setStatus(FileUploadRecord.Status.FAILED);
			databaseService.updateFileUploadRecord(server.getRecordId(), record);
			return CompletableFuture
					.completedFuture(new ProcessResponse(server.getName(), false, server.getRecordId()));
		}

	}

	public boolean sendFile(String filePath, String serverName) {
		try {
			sftpSessionFactory.setThreadKey(serverName);
			File file = new File(filePath);
			if (file.exists()) {
				gateway.upload(file);
				
				// String[] pathArray = filePath.split("/");
				// String fileName = pathArray[pathArray.length-1];
				// logger.info(pathArray[pathArray.length-1]);
				// SftpRemoteFileTemplate template = new SftpRemoteFileTemplate(sftpSessionFactory);
			    // SftpATTRS attrs = sftpSessionFactory.getSession().getClientInstance().
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
