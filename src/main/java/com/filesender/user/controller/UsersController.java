package com.filesender.user.controller;

import com.filesender.security.Constants;
import com.filesender.user.model.RestResponse;
import com.filesender.user.model.User;
import com.filesender.user.repo.UserRepo;
import com.filesender.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class UsersController {

    @Autowired
    UserRepo userRepo;
    @Autowired
    UserService userService;

    @GetMapping()
    public ResponseEntity<?> getUsers() {
        try{
            List<User> userList = userRepo.findAll();

            userList.forEach(user -> {
                user.setRoleId(user.getRole().getId());
            });
            return new ResponseEntity(userList, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody User user, @AuthenticationPrincipal UserDetails userDetails) {
        try{

            user.setCreatedBy(userDetails.getUsername());
            String response = userService.createUser(user);

            if(response != null && response.equals(Constants.Response.SUCCESS.toString())){
                return new ResponseEntity(new RestResponse(false, "User created successfully"), HttpStatus.OK);
            }else {
                return new ResponseEntity(new RestResponse(true, response), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        try{
            String response = userService.updateUser(user);

            if(response != null && response.equals(Constants.Response.SUCCESS.toString())){
                return new ResponseEntity(new RestResponse(false, "User updated successfully"), HttpStatus.OK);
            } else {
                return new ResponseEntity(new RestResponse(true, response), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteUser(@RequestBody User user) {
        try{
            userRepo.delete(user);
            return new ResponseEntity(new RestResponse(false, "User deleted successfully"), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/approve")
    public ResponseEntity<?> approveUser(@RequestBody User user, @AuthenticationPrincipal UserDetails userDetails) {
        try{

            if(user.getCreatedBy() != null && user.getCreatedBy().equals(userDetails.getUsername())){
                return new ResponseEntity(new RestResponse(true, "You can not approve your user creation request"), HttpStatus.OK);
            }


            String response = userService.approveUser(user);

            if(response != null && response.equals(Constants.Response.SUCCESS.toString())){
                return new ResponseEntity(new RestResponse(false, "User approved successfully"), HttpStatus.OK);
            }else {
                return new ResponseEntity(new RestResponse(true, response), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
