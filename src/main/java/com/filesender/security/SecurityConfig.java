package com.filesender.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList("*"));
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Configuration
    @Order(1)
    public class InternalUsersWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Value("${ad.domain}")
        private String AD_DOMAIN;

        @Value("${ad.url}")
        private String AD_URL;

        @Autowired
        private JwtAuthenticationEntryPoint unauthorizedHandler;

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.csrf().disable();

            http.headers().frameOptions().disable();
            http.cors().and().authorizeRequests()
                    .antMatchers(  "/api/recon/upload" ,"/api/token/logout/**", "/api/token", "/api/token/refresh-token",
                            "/api/register/**",  "/api/user/passwordresetrequest", "/ws/**", "/api/token/test", "/h2-console/**", "/favicon.ico")
                    .permitAll().anyRequest().authenticated().and().exceptionHandling()
                    .authenticationEntryPoint(unauthorizedHandler).and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);
        }

        @Override
        @Bean
        public AuthenticationManager authenticationManagerBean() throws Exception {
            return super.authenticationManagerBean();
        }


        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());

        }

        @Bean
        public JwtAuthenticationFilter authenticationTokenFilterBean() throws Exception {
            return new JwtAuthenticationFilter();
        }

        @Bean
        public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
            final ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(
                    AD_DOMAIN, AD_URL);
            provider.setConvertSubErrorCodesToExceptions(true);
            provider.setUseAuthenticationRequestCredentials(true);
            return provider;
        }
    }
}
