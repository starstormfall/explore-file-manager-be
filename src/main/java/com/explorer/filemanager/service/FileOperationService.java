package com.explorer.filemanager.service;

import com.explorer.filemanager.dto.MinioObjectDetails;
import org.springframework.web.multipart.MultipartFile;

public interface FileOperationService {

    MinioObjectDetails createFolder(String bucketName, String objectPath);
}
