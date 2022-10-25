package com.filesender.user.service;

import com.filesender.security.Constants;
import com.filesender.user.model.Role;
import com.filesender.user.model.User;
import com.filesender.user.repo.RoleRepo;
import com.filesender.user.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    @Autowired
    RoleRepo roleRepo;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        final User user = findByUsername(s);

        if (user == null || user.getId() == 0) {
            throw new UsernameNotFoundException("User Not found");
        }

        if (user.getPassword() == null) {
            user.setPassword("");
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                getAuthority());
    }


    public User findByUsername(String username){

        return userRepo.findByUsername(username);
    }

    private List<SimpleGrantedAuthority> getAuthority() {

        final List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));
        return authorities;
    }

    public String createUser(User user){
        try{

            Optional<Role> roleOptional = roleRepo.findById(user.getRoleId());

            if(!roleOptional.isPresent()){
                return String.format("Role with id %s not found", user.getRoleId());
            }

            user.setRole(roleOptional.get());
            user.setActive(1);
            user.setApproved(0);

            User savedUser = userRepo.save(user);

            return Constants.Response.SUCCESS.toString();
        }catch (Exception e){
            log.error("Error occurred while creating user {}", e.getMessage());
            return e.getMessage() != null ? e.getMessage() : "Error occurred while creating user";
        }
    }

    public String updateUser(User user) {
        try{
            Optional<Role> roleOptional = roleRepo.findById(user.getRoleId());
            if(!roleOptional.isPresent()){
                return String.format("Role with id %s not found", user.getRoleId());
            }
            user.setRole(roleOptional.get());
            User savedUser = userRepo.save(user);
            return Constants.Response.SUCCESS.toString();
        }catch (Exception e){
            log.error("Error occurred while calling update user {}", e.getMessage());
            return e.getMessage() != null ? e.getMessage() : "Error occurred while updating user";
        }
    }

    public String approveUser(User user) {
        try{
            Optional<Role> roleOptional = roleRepo.findById(user.getRoleId());
            if(!roleOptional.isPresent()){
                return String.format("Role with id %s not found", user.getRoleId());
            }
            user.setRole(roleOptional.get());
            user.setApproved(1);
            User savedUser = userRepo.save(user);
            return Constants.Response.SUCCESS.toString();
        }catch (Exception e){
            log.error("Error occurred while calling update user {}", e.getMessage());
            return e.getMessage() != null ? e.getMessage() : "Error occurred while updating user";
        }
    }
}
