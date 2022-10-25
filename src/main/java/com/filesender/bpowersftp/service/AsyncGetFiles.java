package com.filesender.bpowersftp.service;

import com.filesender.bpowersftp.model.GetFile;
import com.jcraft.jsch.ChannelSftp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncGetFiles {

    Logger logger = LoggerFactory.getLogger(AsyncGetFiles.class);

    @Value("${bfub_sftp_location}")
    String sftpDirectory;

    @Autowired
    CommonFileUtil commonFileUtil;

    @Async
    public CompletableFuture<Boolean> getRemoteFile (SftpRemoteFileTemplate template,CachingSessionFactory<ChannelSftp.LsEntry> cache,  GetFile getFile) {

        logger.debug("Getting file {}", getFile.getFileName());

        Session<ChannelSftp.LsEntry> session = null;

        try{
            //session = template.getSession();
            session = cache.getSession();
            Long start = System.currentTimeMillis();
            if(session.exists(sftpDirectory + '/' + getFile.getFileName())){
                session.read( sftpDirectory + '/' + getFile.getFileName(), new FileOutputStream(new File(getFile.getFolderName() + "/" + getFile.getFileName())));
            }else {
                logger.error("File {} not found ", getFile.getFileName());
            }
            Long end = System.currentTimeMillis();

            logger.debug("Copy file {}  took {} seconds", getFile.getFileName(), (end-start)/1000 );

        }catch(Exception e){
            logger.error("Error while getting file {}", getFile.getFileName(), e.getMessage(), e);
        }
        finally {
            if(session != null){
                session.close();
            }
        }

        return CompletableFuture.completedFuture(true);
    }
}
