package com.explorer.filemanager.service;

import com.explorer.filemanager.pojo.MinioObjectDetails;

public interface FileOperationService {

    MinioObjectDetails createFolder(String bucketName, String objectPath);
}
