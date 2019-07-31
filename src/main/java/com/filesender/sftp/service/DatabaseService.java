package com.filesender.sftp.service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import com.filesender.sftp.model.FileUploadRecord;

@Service
public class DatabaseService {

	Logger logger = LoggerFactory.getLogger(DatabaseService.class);
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	
	public long createFileUpoadRecord(FileUploadRecord record) {
		try {
			String sql ="INSERT INTO file_upload(branch, status, desc, created_at) VALUES(?, ?, ?, ?)";
			
			Timestamp now = new Timestamp(new Date().getTime());
		
		    KeyHolder keyHolder = new GeneratedKeyHolder();
		    
		    jdbcTemplate.update(connection -> {
		        PreparedStatement ps = connection
		          .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				  ps.setString(1, record.getBranch());
		          ps.setString(2, record.getStatus().toString());
		          ps.setString(3, record.getDesc());
		          ps.setTimestamp(4, now);
		          return ps;
		        }, keyHolder);
		 
		        return (long) keyHolder.getKey();
		}catch(Exception e ) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	
	public long createFileUpoadRecordArchive(FileUploadRecord record) {
		try {
			String sql ="INSERT INTO file_upload_archive(branch, status, desc, created_at, updated_at, file_size) VALUES(?, ?, ?, ?, ?, ?)";
			
		    KeyHolder keyHolder = new GeneratedKeyHolder();
		    
		    jdbcTemplate.update(connection -> {
		        PreparedStatement ps = connection
		          .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
				  ps.setString(1, record.getBranch());
		          ps.setString(2, record.getStatus().toString());
		          ps.setString(3, record.getDesc());
				  ps.setTimestamp(4, record.getCreatedAt());
				  ps.setTimestamp(5, record.getUpdatedAt());
				  ps.setLong(6, record.getFileSize());
		          return ps;
		        }, keyHolder);
		 
		        return (long) keyHolder.getKey();
		}catch(Exception e ) {
			logger.error(e.getMessage(), e);
			return 0;
		}
	}

	public boolean deleteFileUploadRecord(long recordId){
		try{
			String sql= "DELETE FROM file_upload where id =?";
			jdbcTemplate.update(sql, recordId);
			return true;
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public boolean updateFileUploadRecord(long recordId, FileUploadRecord record) {
		try {
			Timestamp now = new Timestamp(new Date().getTime());
			String sql = "UPDATE file_upload set file_size=?, status=?, desc=?, updated_at=? where id=?";
			int no = jdbcTemplate.update(sql, record.getFileSize(), record.getStatus().toString(), record.getDesc(), now, recordId);
			
			if(no> 0) {
				return true;
			}else {
				return false;
			}
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}
	
	public List<FileUploadRecord> getSentFiles() {
		List<FileUploadRecord> records = new ArrayList<>();
		try {
			
			String sql="SELECT * FROM file_upload order by id desc";
			
			records = jdbcTemplate.query(
				    sql, new FileUploadRecordRowMapper());
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
		return records;
	}
	
	public class FileUploadRecordRowMapper implements RowMapper<FileUploadRecord> {
	    @Override
	    public FileUploadRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
	    	FileUploadRecord record = new FileUploadRecord();
	    	record.setId(rs.getLong("id"));
			record.setBranch(rs.getString("branch"));
			record.setFileSize(rs.getLong("file_size"));
	    	record.setDesc(rs.getString("desc"));
	    	record.setStatus(FileUploadRecord.Status.valueOf(rs.getString("status")));
	    	record.setCreatedAt(rs.getTimestamp("created_at"));
	    	record.setUpdatedAt(rs.getTimestamp("updated_at"));
	        return record;
	    }
	}

}
