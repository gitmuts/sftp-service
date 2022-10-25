package com.filesender.security;

public class Constants {
    public enum Response { SUCCESS, FAILED, IN_PROGRESS }

    public static final long   ACCESS_TOKEN_VALIDITY_SECONDS = 300 * 60;
    public static final String SIGNING_KEY                   = "coop4rfvBHU*";
    public static final String TOKEN_PREFIX                  = "Bearer ";
    public static final String HEADER_STRING                 = "Authorization";
}