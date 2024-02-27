package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
