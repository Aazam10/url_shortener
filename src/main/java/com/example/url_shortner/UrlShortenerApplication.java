package com.example.url_shortner;

import com.example.url_shortner.repository.UrlSchemaRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;

@SpringBootApplication
public class UrlShortnerApplication {

	public static void main(String[] args) {

        SpringApplication.run(UrlShortnerApplication.class, args);

//		UrlSchemaRepository schemaRepository = context.getBean(UrlSchemaRepository.class);
//
//		// Create schema (tables)
//		schemaRepository.createSchema();



	}

}


