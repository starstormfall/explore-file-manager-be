package com.explorer.filemanager.controller;

import com.explorer.filemanager.minio.BucketInfo;
import com.explorer.filemanager.minio.MinioAdapter;
import io.minio.messages.Bucket;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public List<BucketInfo> listBuckets() {
        return minioAdapter.getAllBuckets();
    }


    @PutMapping("/bucket/{bucketName}/object")
    public String createObject(@PathVariable("bucketName") String bucketName, @RequestParam("objectPath") String objectPath) {
        return String.valueOf(minioAdapter.createFolderObject(bucketName, objectPath));
    }




}
