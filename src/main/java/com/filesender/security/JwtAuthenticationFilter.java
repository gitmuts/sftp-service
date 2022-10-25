package com.filesender.security;

import com.filesender.user.model.User;
import com.filesender.user.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.filesender.security.Constants.HEADER_STRING;
import static com.filesender.security.Constants.TOKEN_PREFIX;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        req.getHeaderNames();

        final String header = req.getHeader(HEADER_STRING);
        String username = null;
        String authToken = null;
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            authToken = header.replace(TOKEN_PREFIX, "");
            if(authToken != null && authToken !="") {
                try {
                    username = jwtTokenUtil.getUsernameFromToken(authToken);
                } catch (final IllegalArgumentException e) {
                    logger.error("an error occurred during getting username from token {}", e.getCause());
                } catch (final ExpiredJwtException e) {
                    logger.info("User token has expired");
                    username = jwtTokenUtil.getUsernameUnlimitedSkew(authToken);
                    final User user = userService.findByUsername(username);
                    username = null;
                } catch (final SignatureException e) {
                    logger.error("SignatureException. {}", e.getCause());
                }
            }
        } else {
           // logger.warn("couldn't find bearer string, will ignore the header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            final UserDetails userDetails = userDetailsService.loadUserByUsername(username);



            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                // Role role = roleService.getUserRole(userDetails.getUsername());
                final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        chain.doFilter(req, res);
    }
}
