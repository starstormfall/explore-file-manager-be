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
            repository.deleteAll();

            // Seed data

            ObjectId id0 = new ObjectId();
            ObjectId id1 = new ObjectId();
            ObjectId id2 = new ObjectId();
            ObjectId id3 = new ObjectId();

            String workspaceName = faker.cat().breed();
            String firstFolderName = faker.cat().name();
            String secondFolderName = faker.cat().name();

            // root folder
            FileContent rootFolder = new FileContent(
                    id0.toString(),
                    "Workspace: "+ workspaceName,
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "/"+workspaceName+"/",
                    true,
                    false,
                    0,
                    "",
                    ""
            );

            // nested folder in first folder
            FileContent firstNestedFolder = new FileContent(
                    id3.toString(),
                    "Nested",
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "/"+workspaceName+"/"+firstFolderName+"/"+"Nested",
                    false,
                    false,
                    0,
                    "",
                    id1.toString() // parentId
            );

            // first folder
            FileContent firstFolder = new FileContent(
                    id1.toString(),
                    faker.cat().name(),
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "/"+workspaceName+"/"+firstFolderName,
                    true,
                    false,
                    0,
                    "",
                    id0.toString() // parentId
            );
            firstFolder.setData(firstNestedFolder);

            // second folder
            FileContent secondFolder = new FileContent(
                    id2.toString(),
                    faker.cat().name(),
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "/"+workspaceName+"/"+secondFolderName,
                    false,
                    false,
                    0,
                    "",
                    id0.toString() // parentId
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
