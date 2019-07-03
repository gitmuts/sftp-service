package com.filesender.sftp.config;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.DefaultSessionFactoryLocator;
import org.springframework.integration.file.remote.session.DelegatingSessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import com.filesender.sftp.model.ServerInfo;
import com.jcraft.jsch.ChannelSftp.LsEntry;

@Configuration
public class SFTPConfiguration {

	Logger logger = LoggerFactory.getLogger(SFTPConfiguration.class);
	
	
	public static final List<ServerInfo> servers = new ArrayList<>();

	

	@Value("${branches_file_location}")
	String branchesFile;
	
	@Value("${sftp_username}")
	String sftpUsername;
	
	@Value("${sftp_password}")
	String sftpPassword;
	
	@Value("${sftp_upload_folder}")
	String destinationFolder;
	
	

	@Bean
	public DelegatingSessionFactory<LsEntry> sftpSessionFactory() {

		getServersFromFile();
		
		if(servers.size() > 0) {
			logger.info(String.format("%s server info intialized", servers.size()));
		}
		
		Map<String, DefaultSftpSessionFactory> factories = new HashMap<>();


		for(ServerInfo serverInfo : servers) {
			DefaultSftpSessionFactory defaultFactory = new DefaultSftpSessionFactory(true);

			
			// factory.set
			defaultFactory.setHost(serverInfo.getIp());
			defaultFactory.setPort(22);
			defaultFactory.setUser(sftpUsername);
			defaultFactory.setPassword(getDecodedPassword());
			defaultFactory.setAllowUnknownKeys(true);
			
			factories.put(serverInfo.getName(), defaultFactory);
		}
		
		DelegatingSessionFactory<LsEntry> sftpServers = new DelegatingSessionFactory<LsEntry>(
				new DefaultSessionFactoryLocator(factories));

		return sftpServers;
	}

	@Bean
	@ServiceActivator(inputChannel = "toSftpChannel")
	public MessageHandler handler() {
		SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
		handler.setRemoteDirectoryExpression(new LiteralExpression(destinationFolder));
		handler.setFileNameGenerator(new FileNameGenerator() {
			@Override
			public String generateFileName(Message<?> message) {
				if (message.getPayload() instanceof File) {
					return ((File) message.getPayload()).getName();
				} else {
					throw new IllegalArgumentException("File expected as payload.");
				}
			}
		});
		return handler;
	}

	@MessagingGateway
	public interface UploadGateway {
		@Gateway(requestChannel = "toSftpChannel")
		void upload(File file);
	}

	private String getDecodedPassword() {
		byte[] decodedBytes = Base64.getDecoder().decode(sftpPassword);
		String decodedString = new String(decodedBytes);
		return decodedString;
	}

	private List<ServerInfo> getServersFromFile() {
	
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(branchesFile));

			JSONArray jsonArray = (JSONArray) obj;

			Iterator<JSONObject> iterator = jsonArray.iterator();
			while (iterator.hasNext()) {
				JSONObject jsonObj = iterator.next();
				ServerInfo server = new ServerInfo();
				server.setFolderName(jsonObj.get("folder").toString());
				server.setCode(jsonObj.get("code").toString());
				server.setName(jsonObj.get("name").toString());
				server.setIp(jsonObj.get("ip").toString());
				servers.add(server);
			}
		} catch (Exception e) {
			logger.error("Error while loading servers from file " + e.getMessage());
		}
		return servers;
	}
}
