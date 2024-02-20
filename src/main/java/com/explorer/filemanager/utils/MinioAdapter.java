package com.explorer.filemanager.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MinioAdapter {

    private MinioClient minioClient;

    private ObjectMapper mapper;
    public MinioAdapter(MinioClient minioClient, ObjectMapper mapper) {
        this.minioClient = minioClient;
        this.mapper = mapper;
    }

    /**
     *  creates bucket
     * @param bucketName
     * @return
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws XmlParserException
     * @throws InternalException
     */
    public boolean createBucket(String bucketName) {
        try {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            boolean bucketCreated = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

            return bucketCreated;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * To get list of buckets
     * @return
     */
    // list Bucket
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * To upload object
     * @param bucketName
     * @param objectName
     * @param fileName
     * @param contentType
     * @param file
     * @return
     */
    public Map<String, String> uploadFile(String bucketName, String objectName,  String fileName, String contentType, MultipartFile file) {
        try {
            UploadObjectArgs uArgs = UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();

            ObjectWriteResponse resp = minioClient.uploadObject(uArgs);

            Map<String, String> objectDetails = new HashMap<>();
            objectDetails.put("objectName", resp.object());
            objectDetails.put("objectETag", resp.etag());
            objectDetails.put("objectVersionId", resp.versionId());

            return objectDetails;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }



    // download object


}
