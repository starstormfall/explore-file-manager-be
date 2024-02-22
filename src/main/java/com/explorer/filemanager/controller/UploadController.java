package com.explorer.filemanager.controller;

import com.explorer.filemanager.minio.MinioAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

enum UploadAction {
    save,
    keepboth,
    replace
}

@RestController
@RequestMapping(path="api/v1/Upload")
public class UploadController {

    private MinioAdapter minioAdapter;

    public UploadController(MinioAdapter minioAdapter) {
        this.minioAdapter = minioAdapter;
    }



    // Request BODY:
    // - String action : "Save" (save) | "Keep Both" (keepboth) | "Replace" (replace)
    // - String path
    // - List<MultipartFile> (binary) uploadFiles
    // - FileContent data
    // - String fileName
    @GetMapping
    public String uploadFile(@RequestParam("action") String action, @RequestParam("path") String path, @RequestParam("uploadFiles") List<MultipartFile> files) {


        // SAVE action

        // KEEPBOTH action

        // REPLACE action

        String bucketName = "";
        String objectName = path;
        String fileName = "";
        String contentTye = "";




        return "File Uploaded";
    }
}
