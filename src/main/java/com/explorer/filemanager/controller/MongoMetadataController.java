package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="api/v1/admin/mongo")
public class MongoMetadataController {

    private FileContentRepository repository;

    private MongoTemplate mongoTemplate;

    @Autowired
    public MongoMetadataController(FileContentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/cwd/{fileId}")
    public FileContent getCwd(@PathVariable("fileId") String id) {
        FileContent cwd = repository.findById(new ObjectId(id)).get();
        return cwd;
    }

    @GetMapping("/files/{parentId}")
    public FileContent[] getFilesByParentId(@PathVariable("parentId") String id) {
        FileContent[] files = repository.findByParentId(id);
        return files;
    }




}
