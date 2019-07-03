package com.filesender.sftp;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@SpringBootApplication
@EnableAsync
public class SftpApplication {

	@Value("${number_of_threads}")
	private int numberOfThreads;
	
	public static void main(String[] args) {
		SpringApplication.run(SftpApplication.class, args);
	}


	@Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(numberOfThreads);
        executor.setMaxPoolSize(numberOfThreads);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("SFTPSERVICE-");
        executor.initialize();
        return executor;
    }

}
