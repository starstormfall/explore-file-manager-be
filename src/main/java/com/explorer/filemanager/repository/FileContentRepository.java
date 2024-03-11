package com.explorer.filemanager.repository;

import com.explorer.filemanager.model.FileContent;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileContentRepository extends MongoRepository<FileContent, ObjectId> {

    List<FileContent> findByParentId(String parentId);
    FileContent findByMongoId(String mongoId);
    List<FileContent> findByFilterPath(String pathName);
}
