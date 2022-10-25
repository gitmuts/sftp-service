package com.filesender.bpowersftp.service;

import java.util.List;

import com.filesender.bpowersftp.model.FileUploadRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ArchivalService
 */
@Service
public class ArchivalService {

    Logger logger = LoggerFactory.getLogger(ArchivalService.class);

    @Autowired
    DatabaseService databaseService;

    public boolean archiveFileUploadRecords(List<FileUploadRecord> records){
        logger.info("Starting the archival process");
        try{
            for(FileUploadRecord record: records){
                // insert to archive if success delete from file upload
                long result = databaseService.createFileUpoadRecordArchive(record);
                if(result > 0){
                    databaseService.deleteFileUploadRecord(record.getId());
                }
            }
            logger.info("Archival process end");
            return true;
        }catch(Exception e){
            logger.error(e.getMessage(), e);
            return false;
        }

       
    }
}