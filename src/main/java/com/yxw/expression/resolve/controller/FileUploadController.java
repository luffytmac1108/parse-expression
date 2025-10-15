package com.yxw.expression.resolve.controller;

import com.yxw.expression.resolve.service.S3StorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final S3StorageService s3StorageService;

    public FileUploadController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadToS3(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            // 如果文件为空，返回 400 错误，体为 false
            return ResponseEntity.badRequest().body("文件列表为空");
        }

        try {
            // 使用时间戳和文件名组合作为 S3 中的 Key
            String fileKey = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            // 调用整合后的服务方法，上传并获取临时 URL
            String tempFileUrl = s3StorageService.uploadFileAndGetPresignedUrl(
                    fileKey,
                    file.getInputStream(),
                    file.getSize()
            );
            return ResponseEntity.ok(tempFileUrl);
        } catch (Exception e) {
            // 打印异常堆栈以便调试
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("S3 上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("fileKey") String fileKey) {
        // 1. (可选) 检查当前用户是否有权限下载此 fileKey 对应的文件
        // 2. 从 S3StorageService 获取文件内容
        byte[] fileContent = s3StorageService.downloadFile(fileKey);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileKey + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileContent);
    }
}