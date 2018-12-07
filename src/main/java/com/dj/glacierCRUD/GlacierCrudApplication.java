package com.dj.glacierCRUD;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.dj.glacierCRUD.util.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    FileStorageProperties.class
})
public class GlacierCrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlacierCrudApplication.class, args);
	}
}
