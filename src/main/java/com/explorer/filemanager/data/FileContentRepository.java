package com.explorer.filemanager.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileContentRepository extends MongoRepository<FileContent, String> {
}
