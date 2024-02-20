package com.explorer.filemanager.config;

import com.explorer.filemanager.properties.MinioProperties;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    private MinioProperties minioProperties;
    @Autowired
    public MinioConfig(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;
    }
    @Bean
    public MinioClient minioClient() {
        try {
            MinioClient minioClient;
            minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getUrl())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            return minioClient;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
