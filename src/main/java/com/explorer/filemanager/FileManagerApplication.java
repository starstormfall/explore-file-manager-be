package com.explorer.filemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class FileManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
    }

}
