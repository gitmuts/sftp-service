package com.filesender.bpowersftp.repo;

import com.filesender.bpowersftp.model.FileUploadRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FileUploadRecordRepo extends PagingAndSortingRepository<FileUploadRecord, Long> {

    Page<FileUploadRecord> findAllByOrderByIdDesc(Pageable pageable);

    List<FileUploadRecord> findAll();
}
