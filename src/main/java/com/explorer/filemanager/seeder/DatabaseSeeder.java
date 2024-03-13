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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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


            List<ObjectId> objectIdList = new ArrayList<>(13);
            for (int i = 0; i < 13; i++) {
                objectIdList.add(new ObjectId());
            }


            String workspaceName = faker.cat().breed();

            List<String> mainFolderName = new ArrayList<>(3);
            List<FileContent> mainFolders = new ArrayList<>(3);
            for (int i = 1; i < 4; i++) {
                String folderName = faker.cat().name();
                mainFolders.add(new FileContent(
                        objectIdList.get(i).toString(),
                        folderName,
                        folderName,
                        faker.date().past(5, TimeUnit.DAYS).toInstant().toString(),
                        faker.date().past(1, TimeUnit.DAYS).toInstant().toString(),
                        workspaceName+"/",
                        true,
                        false,
                        0,
                        "",
                        objectIdList.get(0).toString() // parentId
                ));
                mainFolderName.add(folderName);
            }

            List<String> firstLevelNestFolderName = new ArrayList<>(3);
            List<FileContent> firstLevelNestedFolders = new ArrayList<>(3);
            for (int i = 4; i < 7; i++) {
                String folderName = faker.color().name();
                firstLevelNestedFolders.add(new FileContent(
                        objectIdList.get(i).toString(),
                        folderName,
                        folderName,
                        faker.date().past(5, TimeUnit.DAYS).toInstant().toString(),
                        faker.date().past(1, TimeUnit.DAYS).toInstant().toString(),
                        workspaceName+"/"+mainFolderName.get(i-4)+"/",
                        (i != 6) ? true : false,
                        false,
                        0,
                        "",
                        objectIdList.get(i-3).toString() // parentId
                ));
                firstLevelNestFolderName.add(folderName);
            }

            List<String> secondLevelNestFolderName = new ArrayList<>(2);
            List<FileContent> secondLevelNestedFolders = new ArrayList<>(2);
            for (int i = 7; i < 9; i++) {
                String folderName = faker.color().name();
                secondLevelNestedFolders.add(new FileContent(
                        objectIdList.get(i).toString(),
                        folderName,
                        folderName,
                        faker.date().past(5, TimeUnit.DAYS).toInstant().toString(),
                        faker.date().past(1, TimeUnit.DAYS).toInstant().toString(),
                        workspaceName+"/"+mainFolderName.get(i-7)+"/"+firstLevelNestFolderName.get(i-7)+"/",
                        (i == 7) ? true : false,
                        false,
                        0,
                        "",
                        objectIdList.get(i-3).toString() // parentId
                ));
                secondLevelNestFolderName.add(folderName);
            }

            FileContent thirdLevelNestedFolder = new FileContent(
                    objectIdList.get(9).toString(),
                    faker.friends().character().toString(),
                    faker.friends().character().toString(),
                    faker.date().past(5, TimeUnit.DAYS).toInstant().toString(),
                    faker.date().past(1, TimeUnit.DAYS).toInstant().toString(),
                    workspaceName+"/"+mainFolderName.get(0)+"/"+firstLevelNestFolderName.get(0)+"/"+secondLevelNestFolderName.get(0)+"/",
                    false,
                    false,
                    0,
                    "",
                    objectIdList.get(7).toString() // parentId
            );


            // root folder
            FileContent rootFolder = new FileContent(
                    objectIdList.get(0).toString(),
                    workspaceName,
                    workspaceName,
                    faker.date().past(5, TimeUnit.DAYS).toString(),
                    faker.date().past(1, TimeUnit.DAYS).toString(),
                    "/",
                    true,
                    false,
                    0,
                    "",
                    ""
            );


            List<FileContent> files = new ArrayList<>();
            files.add(rootFolder);
            files.addAll(mainFolders);
            files.addAll(firstLevelNestedFolders);
            files.addAll(secondLevelNestedFolders);
            files.add(thirdLevelNestedFolder);

            repository.saveAll(files);
        } else {
            log.info("FileContent collection already exists");
        }
    }

}
