package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;

public interface MongoMetadataService {
    FileContent getCwd(String fileId);
    FileContent[] getFilesByParentId(String parentId);
    FileContent createFolder(String folderName, String parentId, String path) throws Exception;
    FileContent[] deleteFiles(String[] names, FileContent[] files) throws Exception;
}
