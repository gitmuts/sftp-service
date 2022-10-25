package com.filesender.user.repo;

import com.filesender.user.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepo extends CrudRepository<User, Long> {

    User findByUsername(String username);

    List<User> findAll();
}
