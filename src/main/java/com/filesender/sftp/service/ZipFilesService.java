package com.filesender.sftp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ZipFilesService {

	Logger logger = LoggerFactory.getLogger(ZipFilesService.class);

	@Autowired
	DatabaseService databaseService;

	public String zipFilesInDirectory(String directory, String code) {
		try {

			List<String> filesToBeZipped = scanDirectory(directory);
			String zipFileName = "mcf" + code + ".zip";
			String zipFileAbsolutePath =  directory + "/" + zipFileName;

			if (filesToBeZipped.size() == 0) {
				logger.info(String.format("No files were found for zipping in %s, checking if file is zipped", directory));
				
				logger.info(zipFileAbsolutePath);

				File zippedFile = new File(zipFileAbsolutePath);

				logger.info("zipped file size {} ", zippedFile.length());
				if(!zippedFile.exists()) {
					logger.info(String.format("%s not found in %s", zippedFile, directory));
					return "";
				}
				
			} else {
				FileOutputStream fos = new FileOutputStream(directory + "/" + zipFileName);
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				for (String srcFile : filesToBeZipped) {
					File fileToZip = new File(directory +"/"+ srcFile);
					FileInputStream fis = new FileInputStream(fileToZip);
					ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
					zipOut.putNextEntry(zipEntry);
	
					byte[] bytes = new byte[1024];
					int length;
					while ((length = fis.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
					fis.close();
	
					fileToZip.delete();
				}
				zipOut.close();
				fos.close();
			}

			return zipFileAbsolutePath;
		} catch (Exception e) {
			logger.error("Error occurred while zipping files for  " + directory + "  " + e.getMessage());
			return "";
		}
	}

	private List<String> scanDirectory(String directory) {
		List<String> filesToBeZipped = new ArrayList<>();
		try {
			File folder = new File(directory);

			if (folder.isDirectory()) {
				File[] files = folder.listFiles();
				for (File file : files) {
					String fileName = file.getName();
					if (!file.isDirectory() && !fileName.endsWith(".zip")) {

						if (fileName.startsWith("mcf") || fileName.endsWith(".sav")
								|| fileName.equals("nad0data.dat")) {
							filesToBeZipped.add(fileName);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(
					String.format("Error occurred while scanning directory for %s , %s", directory, e.getMessage()));
		}
		return filesToBeZipped;
	}
}
