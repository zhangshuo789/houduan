package com.volleyball.volleyballcommunitybackend.controller;

import com.volleyball.volleyballcommunitybackend.dto.response.ApiResponse;
import com.volleyball.volleyballcommunitybackend.dto.response.FileResponse;
import com.volleyball.volleyballcommunitybackend.entity.FileEntity;
import com.volleyball.volleyballcommunitybackend.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String fileType,
            Authentication authentication,
            HttpServletRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "请先登录"));
        }
        Long userId = (Long) authentication.getPrincipal();
        FileResponse fileResponse = fileService.uploadFile(file, fileType, userId, request);
        return ResponseEntity.ok(ApiResponse.success("上传成功", fileResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) throws IOException {
        FileEntity fileEntity = fileService.getFileById(id);
        Resource resource = fileService.getFileResource(id);

        // 对文件名进行UTF-8编码，避免中文乱码
        String encodedFilename = URLEncoder.encode(fileEntity.getFileName(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + encodedFilename + "\"")
                .contentType(MediaType.parseMediaType(fileEntity.getContentType()))
                .body(resource);
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<ApiResponse<String>> getFileUrl(
            @PathVariable Long id,
            HttpServletRequest request) {
        String url = fileService.getFileUrl(id, request);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
