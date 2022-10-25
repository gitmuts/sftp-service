package com.filesender.safaricom.repo;

import com.filesender.safaricom.entity.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FolderRepo extends PagingAndSortingRepository<Folder, Long> {

    Page<Folder>  findAllByOrderByIdDesc(Pageable pageable);

}
