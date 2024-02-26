package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(path="api/v1/admin/mongo")
public class MongoController {

    private FileContentRepository repository;

    private MongoTemplate mongoTemplate;

    @Autowired
    public MongoController(FileContentRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/file/{fileId}")
    public FileContent getCwd(@PathVariable("fileId") String id) {

        FileContent cwd = repository.findById(new ObjectId(id)).get();

        return cwd;
    }




}
