package com.filesender.bpowersftp.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "file_upload")
public class FileUploadRecord {

	public enum Status {SUCCESS, FAILED, IN_PROGRESS};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "branch", nullable = false)
	private String branch;
	@Column(name = "file_size", nullable = false)
	private long fileSize;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private Status status;
	@Column(name = "desc", nullable = false)
	private String desc;
	@CreationTimestamp
	@Column(name = "created_at")
	private Timestamp createdAt;
	@CreationTimestamp
	@Column(name = "updated_at")
	private Timestamp updatedAt;
	
}
