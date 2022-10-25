package com.filesender.safaricom.controller;

import com.filesender.bpowersftp.model.ListResponse;
import com.filesender.safaricom.entity.Folder;
import com.filesender.safaricom.entity.TillRecord;
import com.filesender.safaricom.repo.FolderRepo;
import com.filesender.safaricom.repo.TillRecordRepo;
import com.filesender.user.model.User;
import com.filesender.user.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Slf4j
@RequestMapping("/api/tills")
public class TillRecordsController {

    @Autowired
    TillRecordRepo tillRecordRepo;

    @Autowired
    UserRepo userRepo;

    @Value("${tills_folder}")
    String tillsFolder;

    @Autowired
    FolderRepo folderRepo;

    @GetMapping("/folders")
    public ResponseEntity<?> getFolders(@RequestParam("per_page") int perPage, @RequestParam("page") int page, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            page = page - 1;
            Pageable pageable = PageRequest.of(page, perPage);

            Page<Folder> folderPage;
            folderPage = folderRepo.findAllByOrderByIdDesc(pageable);
            return new ResponseEntity(new ListResponse(folderPage.getContent(), folderPage.getTotalPages(), folderPage.getNumberOfElements()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity("Error occurred, try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping()
    public ResponseEntity<?> getTillRecords(@RequestParam("per_page") int perPage, @RequestParam("page") int page, @RequestParam("folderId") long folderId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            page = page - 1;
            Pageable pageable = PageRequest.of(page, perPage);

            User user = userRepo.findByUsername(userDetails.getUsername());

            Page<TillRecord> tillRecordPage;
            if (user.getRole().getName().equalsIgnoreCase("ADMIN")) {
                tillRecordPage = tillRecordRepo.findAllByFolderIdOrderByIdDesc(folderId, pageable);
            } else {
                tillRecordPage = tillRecordRepo.findAllByFolderIdAndRoleOrderByIdDesc(folderId, user.getRole(), pageable);
            }

            return new ResponseEntity(new ListResponse(tillRecordPage.getContent(), tillRecordPage.getTotalPages(), tillRecordPage.getNumberOfElements()), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity("Error occurred, try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable(name = "fileId") Long fileId) {
        try {

            Optional<TillRecord> optionalTillRecord = tillRecordRepo.findById(fileId);

            if (optionalTillRecord.isPresent()) {

                String filePath = tillsFolder + "/" + optionalTillRecord.get().getFileName();

                log.info("Downloading the file {}", filePath);

                Path path = Paths.get(filePath);


                Resource resource = null;
                try {
                    resource = new UrlResource(path.toUri());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                log.warn("File with id {} not found", fileId);
                return new ResponseEntity("File not found", HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            log.error("Error occurred while calling exportData {} ", e.getMessage(), e);
            return new ResponseEntity("Unknown error occurred, check logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/download_folder/{folderId}")
    public ResponseEntity<?> downloadFolder(@PathVariable(name = "folderId") Long folderId, @AuthenticationPrincipal UserDetails userDetails) {
        try {

            User user = userRepo.findByUsername(userDetails.getUsername());


            LocalDateTime localDate = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String zipFileName = dateTimeFormatter.format(localDate) + user.getId();

            String downloadFolderName = tillsFolder + "/" + zipFileName +".zip";

            List<TillRecord> tillRecords = new ArrayList<>();

            if(user.getRole().equals("ADMIN")){
                tillRecords = tillRecordRepo.findAllByFolderId(folderId);
            } else {
                tillRecordRepo.findAllByFolderIdAndRoleOrderByIdDesc(folderId, user.getRole());
            }

            HashSet<String> hashSet = new HashSet();

            String test = "123";
            if(test.charAt(1) == '1')

            for(TillRecord tillRecord: tillRecords){
                hashSet.add(tillRecord.getFileName());
            }


            FileOutputStream fos = new FileOutputStream(downloadFolderName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for(String fileName: hashSet){

                File fileToZip = new File(tillsFolder +"/"+ fileName);
                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();
            fos.close();

            log.info("Downloading the file {}", downloadFolderName);

            Path path = Paths.get(downloadFolderName);

            Resource resource = null;
            try {
                resource = new UrlResource(path.toUri());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error occurred while calling exportData {} ", e.getMessage(), e);
            return new ResponseEntity("Unknown error occurred, check logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
