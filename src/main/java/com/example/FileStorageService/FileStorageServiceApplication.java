package com.example.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.FileStorageService.Interface.FileUploadServiceInterface;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties(StorageProperties.class)
@OpenAPIDefinition
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = "com.example.FileStorageService")
public class FileStorageServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(com.example.FileStorageService.service.FileUploadService.class);


	public static void main(String[] args) {
		SpringApplication.run(FileStorageServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner init(FileUploadServiceInterface fileService) {
		return (args)-> {
			if (fileService != null) {
			fileService.init();
			} else {
			logger.error("FileService is not injected correctly.");
			}
		};
	}
}