package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;

public interface MongoMetadataService {
    FileContent getCwd(String fileId);
    FileContent[] getFilesByParentId(String parentId);
    FileContent createFolder(String folderName, String parentId, String path) throws Exception;
    FileContent[] deleteFiles(String[] names, FileContent[] files) throws Exception;

    FileContent[] searchFiles(String searchString, String path);

    FileContent[] renameFile(FileContent[] files, String newName) throws Exception;
	
    FileContent[] copyAndMoveFiles(FileContent[] files,FileContent targetedLocation,String targetPath, boolean isRename, String action) throws Exception;

}
