package com.volleyball.volleyballcommunitybackend.service;

import com.volleyball.volleyballcommunitybackend.config.FileProperties;
import com.volleyball.volleyballcommunitybackend.dto.response.FileResponse;
import com.volleyball.volleyballcommunitybackend.entity.FileEntity;
import com.volleyball.volleyballcommunitybackend.repository.FileRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final FileProperties fileProperties;

    public FileService(FileRepository fileRepository, FileProperties fileProperties) {
        this.fileRepository = fileRepository;
        this.fileProperties = fileProperties;
    }

    public FileResponse uploadFile(MultipartFile file, String fileType, Long userId) {
        // 校验文件类型
        List<String> allowedTypes = Arrays.asList(fileProperties.getAllowedTypes().split(","));
        if (!allowedTypes.contains(fileType)) {
            throw new RuntimeException("不支持的文件类型: " + fileType);
        }

        // 校验文件大小
        if (file.getSize() > fileProperties.getMaxSize()) {
            throw new RuntimeException("文件大小超过限制: " + (fileProperties.getMaxSize() / 1024 / 1024) + "MB");
        }

        // 生成存储文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString() + extension;

        // 构建存储路径
        String subDir = fileType.equals("avatar") ? "avatars/user_" + userId : fileType + "/";
        String fullDir = fileProperties.getBasePath() + "/" + subDir;
        String fullPath = fullDir + storedName;

        try {
            // 创建目录
            Files.createDirectories(Paths.get(fullDir));

            // 保存文件
            Path targetPath = Paths.get(fullPath);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 保存记录
            FileEntity fileEntity = new FileEntity();
            fileEntity.setFileName(originalFilename);
            fileEntity.setStoredName(storedName);
            fileEntity.setFilePath(subDir + storedName);
            fileEntity.setFileType(fileType);
            fileEntity.setContentType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setUserId(userId);

            FileEntity saved = fileRepository.save(fileEntity);

            // 返回响应
            return toFileResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }

    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
    }

    public Resource getFileResource(Long id) {
        FileEntity fileEntity = getFileById(id);
        try {
            Path filePath = Paths.get(fileProperties.getBasePath()).resolve(fileEntity.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("文件读取失败");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("文件路径错误");
        }
    }

    public String getFileUrl(Long id) {
        FileEntity fileEntity = getFileById(id);
        return fileProperties.getBaseUrl() + "/" + id;
    }

    private FileResponse toFileResponse(FileEntity fileEntity) {
        String url = fileProperties.getBaseUrl() + "/" + fileEntity.getId();
        return new FileResponse(
                fileEntity.getId(),
                fileEntity.getFileName(),
                url,
                fileEntity.getFileSize(),
                fileEntity.getContentType()
        );
    }
}
