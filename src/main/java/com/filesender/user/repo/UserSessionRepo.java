package com.filesender.user.repo;

import com.filesender.user.model.User;
import com.filesender.user.model.UserSession;
import org.springframework.data.repository.CrudRepository;

public interface UserSessionRepo extends CrudRepository<UserSession, Long> {

    UserSession findByUser(User admin);
}
