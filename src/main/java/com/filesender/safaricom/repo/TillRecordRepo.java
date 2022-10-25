package com.filesender.safaricom.repo;

import com.filesender.safaricom.entity.TillRecord;
import com.filesender.user.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TillRecordRepo extends PagingAndSortingRepository<TillRecord, Long> {

    Page<TillRecord>  findAllByFolderIdAndRoleOrderByIdDesc(long folderId, Role role, Pageable pageable);

    Page<TillRecord>  findAllByFolderIdOrderByIdDesc(long folderId, Pageable pageable);

    List<TillRecord> findAllByFolderId(long folderId);

    List<TillRecord>  findAllByFolderIdAndRoleOrderByIdDesc(long folderId, Role role);

}
