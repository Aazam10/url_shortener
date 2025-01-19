package com.example.url_shortner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class UrlShortenerApplication {

	public static void main(String[] args) {

        SpringApplication.run(UrlShortenerApplication.class, args);


	}

}


