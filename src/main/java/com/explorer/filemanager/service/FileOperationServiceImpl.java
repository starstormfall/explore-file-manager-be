package com.explorer.filemanager.service;

import com.explorer.filemanager.dto.MinioObjectDetails;
import com.explorer.filemanager.repository.FileContentRepository;
import com.explorer.filemanager.minio.MinioAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class FileOperationServiceImpl implements FileOperationService {

    private FileContentRepository repository;

    private MinioAdapter minioAdapter;

    private MongoTemplate mongoTemplate;

    @Autowired
    public FileOperationServiceImpl(FileContentRepository repository, MongoTemplate mongoTemplate, MinioAdapter minioAdapter) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
        this.minioAdapter = minioAdapter;
    }

    public MinioObjectDetails createFolder(String bucketName, String objectPath ) {
        MinioObjectDetails newFolder = minioAdapter.createFolderObject(bucketName, objectPath);
        return newFolder;
    }
}
