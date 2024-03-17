package com.explorer.filemanager.service;

import com.explorer.filemanager.model.FileContent;

import java.util.List;

public interface MongoMetadataService {
    FileContent getCwd(String fileId);
    List<FileContent> getFilesByParentId(String parentId);
    FileContent createFolder(String folderName, String parentId, String path) throws Exception;
    FileContent renameFile(FileContent file, String newName) throws Exception;
    List<FileContent> deleteFiles(String[] names, List<FileContent> files) throws Exception;
    List<FileContent> searchFiles(String searchString, String path);
    List<FileContent> copyFiles(String[] names, List<FileContent> files, String oldPath, String targetPath, FileContent targetData, String[] renameFiles) throws Exception;
    List<FileContent> moveFiles(String[] names, List<FileContent> files, String oldPath, String targetPath, FileContent targetData, String[] renameFiles) throws Exception;

}
