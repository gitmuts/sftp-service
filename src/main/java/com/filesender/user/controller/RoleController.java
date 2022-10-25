package com.filesender.user.controller;

import com.filesender.user.model.RestResponse;
import com.filesender.user.model.Role;
import com.filesender.user.repo.RoleRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/role")
@RestController
@Slf4j
public class RoleController {

    @Autowired
    RoleRepo roleRepo;

    @GetMapping()
    public ResponseEntity<?> getRoles() {
        try{
            return new ResponseEntity(roleRepo.findAll(), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping()
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        try{
           Role savedRole = roleRepo.save(role);
           if(savedRole != null){
               return new ResponseEntity(new RestResponse(false, "Role created successfully"), HttpStatus.OK);
           } else {
               return new ResponseEntity(new RestResponse(false, "Failed to create role"), HttpStatus.OK);
           }

        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateRole(@RequestBody Role role) {
        try{
            Role savedRole = roleRepo.save(role);
            if(savedRole != null){
                return new ResponseEntity(new RestResponse(false, "Role updated successfully"), HttpStatus.OK);
            } else {
                return new ResponseEntity(new RestResponse(false, "Failed to update role"), HttpStatus.OK);
            }

        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteRole(@RequestBody Role role) {
        try{
            roleRepo.delete(role);
            return new ResponseEntity(new RestResponse(false, "Role deleted successfully"), HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity(new RestResponse(true, e.getMessage() != null ? e.getMessage() : "Error occurred, try later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
