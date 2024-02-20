package com.explorer.filemanager.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("minio.config")
@Getter
@Setter
public class MinioProperties {

    private String url;
    private String accessKey;
    private String secretKey;

}
