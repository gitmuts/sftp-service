package com.filesender.user.controller;

import com.filesender.security.JwtTokenUtil;
import com.filesender.user.model.*;
import com.filesender.user.repo.UserRepo;
import com.filesender.user.repo.UserSessionRepo;
import com.filesender.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/token")
@Slf4j
public class AuthenticationController {

    @Autowired
    UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserSessionRepo userSessionRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping
    public ResponseEntity<?> generateToken(@RequestBody Login login) {
        try {

            User user = userRepo.findByUsername(login.getUsername());

            if (user == null) {
                log.info("User {} not found", login.getUsername());

                return new ResponseEntity<>(new RestResponse(true, "User not Found."), HttpStatus.UNAUTHORIZED);
            }


            if(user.getActive() != 1){
                return new ResponseEntity<>(new RestResponse(true, "User is not active."), HttpStatus.UNAUTHORIZED);
            }

            if(user.getApproved() != null && user.getApproved() != 1){
                return new ResponseEntity<>(new RestResponse(true, "User creation request not approved."), HttpStatus.UNAUTHORIZED);
            }


            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserSession userSession = userSessionRepo.findByUser(user);

            if(userSession ==  null){
                userSession = new UserSession();
                userSession.setUser(user);
                userSession.setLoggedIn(0);
                userSessionRepo.save(userSession);
            }

            String token = jwtTokenUtil.generateToken(user);

            userSession.setLoggedIn(1);
            userSessionRepo.save(userSession);

            return new ResponseEntity(new LoginToken(user, token), HttpStatus.OK);
        } catch (AuthenticationException authe){
            log.error("Authentication error for  {} Ex: {}", login.getUsername(), authe.getMessage());
            return new ResponseEntity<>(new RestResponse(true, "Wrong username/Password."), HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            log.error("Error occurred while calling generateToken ", e);
            return new ResponseEntity(new RestResponse(true, "Error occurred, try again later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/logout/{token}")
    public ResponseEntity<?> logout(@PathVariable("token") String token) {
        try{

            if(token == null || token.equals("")){
                return new ResponseEntity<>(new RestResponse(true, "Failed to logout, token is empty"), HttpStatus.OK);
            }

            String username = jwtTokenUtil.getUsernameUnlimitedSkew(token);
            User user = userService.findByUsername(username);
            if(log.isDebugEnabled()){
                log.debug("Received a request to log out {}", username);
            }
            UserSession userSession = userSessionRepo.findByUser(user);

            if(userSession == null){
                return new ResponseEntity<>(new RestResponse(false, "User session not found"), HttpStatus.OK);
            }

            userSession.setLoggedIn(0);
            userSessionRepo.save(userSession);
            return new ResponseEntity<>(new RestResponse(false, "User logged out"), HttpStatus.OK);
        }catch (Exception e){
            log.error("Error occurred while calling {} for {} Ex: {}", "logout", token, e);
            return new ResponseEntity<>(new RestResponse(true, "Failed to Logout, Try Later"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
