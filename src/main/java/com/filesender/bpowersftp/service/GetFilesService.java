package com.filesender.bpowersftp.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.filesender.bpowersftp.model.GetFile;
import com.filesender.bpowersftp.model.ServerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

@Service
public class GetFilesService {

    Logger logger = LoggerFactory.getLogger(GetFilesService.class);

    @Value("${bfub_sftp_host}")
    String sftpHost;

    @Value("${bfub_sftp_username}")
    String sftpUsername;

    @Value("${bfub_sftp_password}")
    String sftpPassword;

    @Value("${sftp_common_folder}")
    String commonDir;

    @Autowired
    AsyncGetFiles asyncGetFiles;

    @Autowired
    CommonFileUtil commonFileUtil;

    public boolean getFiles(List<ServerInfo> servers) {

        long startTime = System.currentTimeMillis();

        try{

        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(22);
        factory.setUser(sftpUsername);
        factory.setPassword(getDecodedPassword());
        factory.setAllowUnknownKeys(true);
        factory.setTimeout(5000);

        CachingSessionFactory cache = new CachingSessionFactory<>(factory);


        SftpRemoteFileTemplate template = new SftpRemoteFileTemplate(factory);

        boolean commonFiles = commonFileUtil.getCommonFiles(template);


        if(!commonFiles){
            logger.error("Failed to get common files, sftp operation exiting");
            return false;
        }

        //get specific files
        List<GetFile> getFilesList = new ArrayList<>();

        for(ServerInfo server: servers){
            GetFile getFilea = new GetFile();
            String fileNamea= "mcfa0" + server.getCode() + ".dat";
            getFilea.setFileName(fileNamea);
            getFilea.setFolderName(server.getFolderName());
            getFilesList.add(getFilea);


            GetFile getFilec = new GetFile();
            String fileNamec= "mcfc0" + server.getCode() + ".dat";
            getFilec.setFileName(fileNamec);
            getFilec.setFolderName(server.getFolderName());
            getFilesList.add(getFilec);
        }


        List<CompletableFuture<Boolean>> responses = new ArrayList<>();

        for(GetFile getFile: getFilesList){

        CompletableFuture<Boolean>  getFileResponse = asyncGetFiles.getRemoteFile(template,cache, getFile);
            responses.add(getFileResponse);
        }

        CompletableFuture.allOf(responses.toArray(new CompletableFuture[responses.size()])).join();

         //copy files
         //copy from common to all the branch folders

         for(ServerInfo server: servers){
            commonFileUtil.copyfiles(server.getFolderName());
         }

        Long endTime = System.currentTimeMillis();



        logger.info("Copying of files from BFUB took {} seconds", (endTime - startTime) /1000);
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    private String getDecodedPassword() {
        byte[] decodedBytes = Base64.getDecoder().decode(sftpPassword);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }
}
