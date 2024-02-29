package com.explorer.filemanager.minio;

import com.explorer.filemanager.minio.MinioAdapter;
import com.explorer.filemanager.service.MongoMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MongoAndMinioTransactionService {
    private MongoMetadataService mongoMetadataService;
    private MinioAdapter minioAdapter;
    @Autowired
    public MongoAndMinioTransactionService(MongoMetadataService mongoMetadataService, MinioAdapter minioAdapter){
        this.mongoMetadataService = mongoMetadataService;
        this.minioAdapter = minioAdapter;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createFolderTransaction(String workspaceId, String folderName, String parentId, String path) throws Exception {
        try {
            mongoMetadataService.createFolder(folderName, parentId, path);
            minioAdapter.createFolderObject(workspaceId, path);
            // Upload file to MinIO

        } catch (Exception e) {
            // If any operation fails, perform rollback
            rollbackOperations(folderName, path);
            throw e;
        }
    }

    private void rollbackOperations(String folderName, String path) {
        // Rollback MongoDB operation to destroy Document

        // Rollback MinIO operation (if applicable) to delete object path and files
        // You may need to implement a compensation mechanism to handle MinIO rollback.
    }


}
