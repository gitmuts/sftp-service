package com.filesender.safaricom.entity;


import com.filesender.user.model.Role;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "till_record")
public class TillRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="role_id")
    private Role role;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="folder_id")
    private Folder folder;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;
}
