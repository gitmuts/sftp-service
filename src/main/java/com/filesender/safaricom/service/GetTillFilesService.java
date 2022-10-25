package com.filesender.safaricom.service;

import com.filesender.safaricom.entity.Folder;
import com.filesender.safaricom.entity.TillRecord;
import com.filesender.safaricom.repo.FolderRepo;
import com.filesender.safaricom.repo.TillRecordRepo;
import com.filesender.user.model.Role;
import com.filesender.user.repo.RoleRepo;
import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class GetTillFilesService {

    @Value("${safaricom_sftp_host}")
    String sftpHost;

    @Value("${safaricom_sftp_port}")
    int sftpPort;

    @Value("${safaricom_sftp_username}")
    String sftpUsername;

    @Value("${safaricom_sftp_password}")
    String sftpPassword;

    @Value("${safaricom_sftp_location}")
    String safaricomDirectory;

    @Value("${datacenter_tills}")
    String dataCenterTillsString;

    @Value("${dps_tills}")
    String dpsTillsString;

    @Value("${contactcenter_tills}")
    String contactCenterTillsString;

    @Value("${cou_tills}")
    String couTillsString;

    @Value("${tills_folder}")
    String tillsFolder;

    @Autowired
    TillRecordRepo tillRecordRepo;
    @Autowired
    RoleRepo roleRepo;
    @Autowired
    FolderRepo folderRepo;

    @Scheduled(cron = "${cron.safaricom_files.expression}")
    public void getTillFiles(){
        try{
            LocalDate localDate = LocalDate.now().minusDays(1);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            String folderName = localDate.format(dateTimeFormatter);

            folderName = folderName + "23";

            Session<ChannelSftp.LsEntry> session = getSftpTemplate();

            String baseDirectory = safaricomDirectory + "/" + folderName;

            Folder folder = new Folder();
            folder.setName(folderName);
            folderRepo.save(folder);

            if(session.exists(baseDirectory)){
                log.info("{} folder exists...scanning", folderName);
                String[] files = session.listNames(baseDirectory);

                log.info("Found files {}", Arrays.asList(files));

                List<String> dataCenterTills = getTillsAsList(dataCenterTillsString);
                List<String> dpsTills = getTillsAsList(dpsTillsString);
                List<String> contactCenterTills = getTillsAsList(contactCenterTillsString);
                List<String> couTills = getTillsAsList(couTillsString);

                Role dcRole = roleRepo.findByName("DATA_CENTER");
                Role dpsRole = roleRepo.findByName("DPS");
                Role ccRole = roleRepo.findByName("CONTACT_CENTER");
                Role couRole = roleRepo.findByName("COU");
                Role adminRole = roleRepo.findByName("ADMIN");


                for(String file: files){
                    session.read(baseDirectory + "/"+ file, new FileOutputStream(new File(tillsFolder + "/" + file)));
                    String[] tillArray = file.split("_");


                    if(dataCenterTills.contains(tillArray[1])){
                        createRecordForTill(file, dcRole, folder);
                    }

                    if(dpsTills.contains(tillArray[1])){
                        createRecordForTill(file, dpsRole, folder);
                    }

                    if(contactCenterTills.contains(tillArray[1])){
                        createRecordForTill(file, ccRole, folder);
                    }

                    if(couTills.contains(tillArray[1])){
                        createRecordForTill(file, couRole, folder);
                    }
                }
            } else {
                log.warn("FOLDER {} not found", baseDirectory);
            }

            //folder
            //list files in folder

        }catch (Exception e){
            log.error("Error occurred while calling  getFiles", e);
        }
    }

    private Session getSftpTemplate() {
        try{
            DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
            factory.setHost(sftpHost);
            factory.setPort(sftpPort);
            factory.setUser(sftpUsername);
            factory.setAllowUnknownKeys(true);
            factory.setPassword(getDecodedPassword());
            factory.setTimeout(5000);

            CachingSessionFactory cache = new CachingSessionFactory<>(factory);
            SftpRemoteFileTemplate template = new SftpRemoteFileTemplate(factory);

            return cache.getSession();
        } catch (Exception e){
            log.error("Error occurred while calling getSftpTemplate ", e);
            return null;
        }
    }

    private String getDecodedPassword() {
        byte[] decodedBytes = Base64.getDecoder().decode(sftpPassword);
        String decodedString = new String(decodedBytes);
        return decodedString;
    }

    private List<String> getTillsAsList(String tillsString){
        return Arrays.asList(tillsString.split(","));
    }

    private void createRecordForTill(String fileName, Role role, Folder folder){
        TillRecord tillRecord = new TillRecord();
        tillRecord.setFileName(fileName);
        tillRecord.setRole(role);
        tillRecord.setFolder(folder);
        tillRecordRepo.save(tillRecord);
    }
}
