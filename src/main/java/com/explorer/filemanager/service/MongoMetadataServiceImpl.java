package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Slf4j
@Service
public class MongoMetadataServiceImpl implements MongoMetadataService {

    private FileContentRepository repository;

    private MongoTemplate mongoTemplate;

    @Autowired
    public MongoMetadataServiceImpl(FileContentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get cwd by id
     * @param fileId
     * @return
     */
    @Override
    public FileContent getCwd(String fileId) {
        FileContent cwd = repository.findById(new ObjectId(fileId)).get();
        return cwd;
    }

    /**
     * Get all files with parentId
     * @param parentId
     * @return
     */
    @Override
    public FileContent[] getFilesByParentId(String parentId) {
        FileContent[] files = repository.findByParentId(parentId);
        return files;
    }

    /**
     * creates a new folder entry
     * @param folderName
     * @param parentId
     * @param path
     * @return
     */
    @Override
    public FileContent createFolder(String folderName, String parentId, String path) throws Exception {

        // need to check if folder with same name already exists with parentId
        // if already exists, throw error
        // else can create new folder
        Query query = new Query(
                Criteria.where("parentId").is(parentId)
                        .andOperator(Criteria.where("name").is(folderName)));

        if (mongoTemplate.findOne(query, FileContent.class) == null) {
            ObjectId id = new ObjectId();
            FileContent newFolder = new FileContent(
                    id.toString(),
                    folderName,
                    folderName,
                    Instant.now().toString(),
                    Instant.now().toString(),
                    path,
                    false,
                    false,
                    0,
                    "",
                    parentId // parentId
            );

            FileContent newFolderData = repository.save(newFolder);
            return newFolderData;
        } else {
            throw new Exception("Folder with same name exists");
        }
    };

}
