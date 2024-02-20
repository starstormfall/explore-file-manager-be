package com.explorer.filemanager.repository;

import com.explorer.filemanager.model.FileContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileContentRepository extends MongoRepository<FileContent, String> {
}
