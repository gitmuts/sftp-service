package com.filesender.user.repo;

import com.filesender.user.model.Role;
import org.springframework.data.repository.CrudRepository;

public interface RoleRepo extends CrudRepository<Role, Long> {

    Role findByName(String name);
}
