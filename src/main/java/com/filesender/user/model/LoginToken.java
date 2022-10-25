package com.filesender.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginToken {

    private User user;
    private String token;
}
