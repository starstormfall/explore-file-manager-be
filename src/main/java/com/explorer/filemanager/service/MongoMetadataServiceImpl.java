package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MongoMetadataServiceImpl implements MongoMetadataService {

    private FileContentRepository repository;

    @Autowired
    public MongoMetadataServiceImpl(FileContentRepository repository) {
        this.repository = repository;
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

    @Override
    public String createFolder(String folderName, String parentId, String path) {

        ObjectId id = new ObjectId();
        FileContent newFolder = new FileContent(
                id.toString(),
                folderName,
                folderName,
                Instant.now().toString(),
                Instant.now().toString(),
                path + id + "/",
                false,
                false,
                0,
                "",
                parentId // parentId
        );

        return String.format("New folder created: %s", folderName);
    };

}
