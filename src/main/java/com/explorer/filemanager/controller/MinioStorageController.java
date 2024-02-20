package com.explorer.filemanager.controller;

import com.explorer.filemanager.utils.MinioAdapter;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
public class MinioStorageController {

    private MinioAdapter minioAdapter;

    public MinioStorageController(MinioAdapter minioAdapter) {
        this.minioAdapter = minioAdapter;
    }

    @PutMapping("/bucket")
    public String createBucket(@RequestParam("name") String bucketName ) {
        try {
            minioAdapter.createBucket(bucketName);
            return "";
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    @GetMapping( "/buckets")
    public List<Bucket> listBuckets() {
        return minioAdapter.getAllBuckets();
    }




}
