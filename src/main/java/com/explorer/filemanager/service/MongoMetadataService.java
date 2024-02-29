package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;

public interface MongoMetadataService {
    FileContent getCwd(String fileId);
    FileContent[] getFilesByParentId(String parentId);
    String createFolder(String folderName, String parentId, String path);
}
