package com.explorer.filemanager.minio;

import com.explorer.filemanager.pojo.MinioObjectDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.messages.Bucket;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;


@Service
public class MinioAdapter {
    private MinioClient minioClient;
    private ObjectMapper mapper;
    public MinioAdapter(MinioClient minioClient, ObjectMapper mapper) {
        this.minioClient = minioClient;
        this.mapper = mapper;
    }

    /**
     * Creates new bucket
     * @param bucketName
     * @return
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
     * Lists all buckets
     * @return
     */
    // list Bucket
    public List<BucketInfo> getAllBuckets() {
        try {

            List<BucketInfo> bucketInfoList = new ArrayList<>();

            List<Bucket> bucketList = minioClient.listBuckets();
            for (Bucket bucket : bucketList) {
                bucketInfoList.add(new BucketInfo(bucket.name(), bucket.creationDate()));
            }

            return bucketInfoList;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * Uploads object with file (binary)
     * @param bucketName
     * @param objectName
     * @param fileName
     * @param contentType
     * @param file
     * @return
     */
    public MinioObjectDetails uploadFile(String bucketName, String objectName,  String fileName, String contentType, MultipartFile file) {
        try {
            UploadObjectArgs uArgs = UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();

            ObjectWriteResponse resp = minioClient.uploadObject(uArgs);

            MinioObjectDetails details = new MinioObjectDetails();
            details.setName(resp.object());
            details.setETag(resp.etag());
            details.setVersionId(resp.versionId());

            return details;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * Creates new empty folder/directory
     * @param bucketName
     * @param objectPath
     * @return
     */
    public MinioObjectDetails createFolderObject(String bucketName, String objectPath) {
        try {
            ObjectWriteResponse resp = minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectPath)
                            .stream(new ByteArrayInputStream(new byte[] {}), 0, -1)
                            .build());

            MinioObjectDetails details = new MinioObjectDetails();
            details.setName(resp.object());
            details.setETag(resp.etag());
            details.setVersionId(resp.versionId());

            return details;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }


    // download object




}
