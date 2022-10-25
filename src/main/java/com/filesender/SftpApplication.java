package com.filesender;

import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;

import com.filesender.user.model.Role;
import com.filesender.user.model.User;
import com.filesender.user.repo.RoleRepo;
import com.filesender.user.repo.UserRepo;
import org.apache.tomcat.util.buf.UEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SftpApplication {

    Logger logger = LoggerFactory.getLogger(SftpApplication.class);

    @Autowired
    UserRepo userRepo;
    @Autowired
    RoleRepo roleRepo;

	@Value("${get_number_of_threads}")
    private int getNumberOfThreads;
    
    @Value("${send_number_of_threads}")
	private int sendNumberOfThreads;
	
	public static void main(String[] args) {
		SpringApplication.run(SftpApplication.class, args);
	}

    @PostConstruct
    public void init() {

        TimeZone.setDefault(TimeZone.getTimeZone("EAT"));

        
        logger.debug("Date in EAT: " + new Date().toString());
        
    }

	@Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(getNumberOfThreads);
        executor.setMaxPoolSize(getNumberOfThreads);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("GETSERVICE-");
        executor.initialize();
        return executor;
    }


    @Bean
    public Executor sendExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(sendNumberOfThreads);
        executor.setMaxPoolSize(sendNumberOfThreads);
        executor.setQueueCapacity(10000);
        executor.setThreadNamePrefix("SENDSERVICE-");
        executor.initialize();
        return executor;
    }


    @Bean
    public void createUser(){
        String username = "gkipkemoi";

        User user = userRepo.findByUsername(username);

        Role adminRole = roleRepo.findByName("ADMIN");

        if(adminRole == null) {
            adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepo.save(adminRole);
        }

        if(user == null){
            user = new User();
            user.setName("Gideon Mutai");
            user.setUsername("gkipkemoi");
            user.setRole(adminRole);
            userRepo.save(user);
        }
    }

    @Bean
    public void  initRoles(){
        String[] roles = {"DATA_CENTER", "DPS", "CONTACT_CENTER", "COU", "ADMIN", "SERVICE_DESK"};
        for(String roleString: roles){
            Role role = roleRepo.findByName(roleString);
            if(role == null){
                role = new Role();
                role.setName(roleString);
                roleRepo.save(role);
            }
        }
    }
}
