package com.explorer.filemanager.controller;

import com.explorer.filemanager.model.FileContent;
import com.explorer.filemanager.repository.FileContentRepository;
import com.explorer.filemanager.service.MongoMetadataServiceImpl;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="api/v1/admin/mongo")
public class MongoMetadataController {

    private MongoMetadataServiceImpl mongoMetadataService;

    @Autowired
    public MongoMetadataController(MongoMetadataServiceImpl mongoMetadataService) {
        this.mongoMetadataService = mongoMetadataService;
    }

    @GetMapping("/cwd/{fileId}")
    public FileContent getCwd(@PathVariable("fileId") String id) {
        return mongoMetadataService.getCwd(id);
    }

    @GetMapping("/files/{parentId}")
    public List<FileContent> getFilesByParentId(@PathVariable("parentId") String id) {
        return mongoMetadataService.getFilesByParentId(id);
    }




}
