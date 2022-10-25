package com.filesender.bpowersftp.service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

/**
 * CommonFileUtil
 */
@Service
public class CommonFileUtil {

    Logger logger = LoggerFactory.getLogger(CommonFileUtil.class);

    @Value("${bfub_sftp_location}")
    String sftpDirectory;

    @Value("${sftp_common_folder}")
    String commonDir;

    public boolean getCommonFiles(SftpRemoteFileTemplate template) {

        List<String> commonFiles = new ArrayList<>();
        commonFiles.add("mcd0data.sav");
        commonFiles.add("nad0data.dat");
        commonFiles.add("mcfstpch.dat");

        boolean commonFilesExist = commonFilesExist(commonFiles);

        if(commonFilesExist){
            logger.info("Common files exist");
            return true;
        }else {
            logger.info("Common files do not exist, getting them from bfub");
        }

        Session<ChannelSftp.LsEntry> session = null;

        try{
            session = template.getSession();

            for(String commonFile : commonFiles){
                Long start = System.currentTimeMillis();
                session.read( sftpDirectory + '/' + commonFile, new FileOutputStream(new File(commonDir + "/" + commonFile)));
                Long end = System.currentTimeMillis();
                logger.info("Copy file {}  took {} seconds", commonFile, (end-start)/1000 );
            }
        return true;
        }catch(Exception e){
            logger.error("Error while getting common files ", e);
            return false;
        }finally {
            if(session != null){
                session.close();
            }
        }
    }

    private boolean commonFilesExist(List<String> commonFiles) {
        try{
            for(String commonFile : commonFiles){
                
                File file = new File(commonDir + "/"+ commonFile);
                    if(!file.exists()){
                        return false;
                    }
                }
            return true;
        }catch(Exception e){
            logger.error("Error while checking if common files exist", e);
            return false;
        }
    }

    public boolean copyfiles(String folderName) {
        FileOutputStream out = null;
        try{
            List<String> commonFiles = new ArrayList<>();
            commonFiles.add("mcd0data.sav");
            commonFiles.add("nad0data.dat");
            commonFiles.add("mcfstpch.dat");

            

            for(String file: commonFiles){

                String source = commonDir + "/" + file;
                File sourceFile = new File(source);
                String dest = folderName + "/" + file;

                Files.copy(sourceFile.toPath(),
                (new File(dest)).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            return false;
        }finally {
            if(out != null){
                try{
                    out.close();
                }catch(Exception e){

                }
            }   
        }
       
    }
}