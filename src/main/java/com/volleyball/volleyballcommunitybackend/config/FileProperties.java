package com.volleyball.volleyballcommunitybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private String basePath = "/data/volleyball-community";
    private String baseUrl = "http://localhost:8080/api/file";
    private long maxSize = 10485760L; // 10MB
    private String allowedTypes = "avatar,post_image";

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public String getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(String allowedTypes) {
        this.allowedTypes = allowedTypes;
    }
}
