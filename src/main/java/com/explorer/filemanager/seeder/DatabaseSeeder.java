package com.explorer.filemanager.seeder;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {
    Faker faker = new Faker();
    private final FileContentRepository repository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public DatabaseSeeder(FileContentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if the collection exists
        if (!mongoTemplate.collectionExists("FileContent")) {

            mongoTemplate.createCollection("FileContent");
            log.info("No existing FileContent collection. Initialized FileContent collection successfully!");

//          Seed data
            repository.deleteAll();

            // root folder
            ObjectId id0 = new ObjectId();
            FileContent rootFolder = new FileContent(
                    id0.toString(),
                    "Files",
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "",
                    true,
                    false,
                    0,
                    ""
            );

            // nested folder in first folder
            ObjectId id3 = new ObjectId();
            FileContent firstNestedFolder = new FileContent(
                    id3.toString(),
                    "first-nested-folder",
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "\\\\first-folder\\\\",
                    false,
                    false,
                    0,
                    ""
            );

            // first folder
            ObjectId id1 = new ObjectId();
            FileContent firstFolder = new FileContent(
                    id1.toString(),
                    "first-folder",
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "\\\\",
                    true,
                    false,
                    0,
                    ""
            );
            firstFolder.setData(firstNestedFolder);

            // second folder
            ObjectId id2 = new ObjectId();
            FileContent secondFolder = new FileContent(
                    id2.toString(),
                    "second-folder",
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "\\\\",
                    false,
                    false,
                    0,
                    ""
            );

            FileContent[] files = {
                    rootFolder,
                    firstFolder,
                    secondFolder,
                    firstNestedFolder
            };



            repository.saveAll(Arrays.asList(files));
        } else {
            log.info("FileContent collection already exists");
        }
    }

}
