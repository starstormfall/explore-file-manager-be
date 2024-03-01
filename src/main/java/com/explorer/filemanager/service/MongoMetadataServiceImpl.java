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
import java.util.ArrayList;
import java.util.List;


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
     * creates a new document (folder entry)
     * updates parent folder to hasChild so that nested folder can show up in frontend tree view
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

            FileContent parentFolder = repository.findById(new ObjectId(parentId)).get();
            parentFolder.setHasChild(true);
            repository.save(parentFolder);

            return newFolderData;
        } else {
            throw new Exception("Folder with same name exists");
        }
    }

    /**
     * Delete files given a list of filenames
     * Also deletes the nested folders and files
     * @param files
     * @return list of remaining files with same parentId after deletion
     * @throws Exception
     */
    @Override
    public FileContent[] deleteFiles(FileContent[] files) throws Exception {

        // all files will share same parentId because only files under the same parent folder can be deleted together
        String parentId = files[0].getParentId();
        List<String> fileNames = new ArrayList<>();
        List<String> fileIds = new ArrayList<>();

        for (FileContent file : files) {
            fileNames.add(file.getName());
            fileIds.add(file.getMongoId());
        }

        log.info(fileNames.toString());
        log.info(fileIds.toString());

        Criteria criteria = new Criteria().orOperator(
            // get selected files with matching fileName and matching parentId
            Criteria.where("name").in(fileNames).andOperator(Criteria.where("parentId").is(parentId)),
            // get child of selected files with child's parentId that matches the mongoId of the files to be deleted
            Criteria.where("parentId").in(fileIds)
        );

        Query query = new Query(criteria);

        mongoTemplate.findAllAndRemove(query, FileContent.class);

        FileContent[] existingFilesAfterDeletion = repository.findByParentId(parentId);
        return existingFilesAfterDeletion;

    }


}
