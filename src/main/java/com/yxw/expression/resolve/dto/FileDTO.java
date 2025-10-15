package com.yxw.expression.resolve.dto;

import java.time.Instant;
import java.util.List;

public class FileDTO {
    private String fileKey;         // 文件在 S3 中的完整路径 (Key)
    private String fileName;        // 文件名（从 Key 中提取）
    private Instant lastModified;     // 最后修改时间
    private Long sizeBytes;         // 文件大小
    private String tempImageUrl;    // 临时图片访问 URL

    // 新增字段
    private boolean isImage;
    private String fileExtension;

    // 常见的图片扩展名列表（您可以根据需要扩展）
    private static final List<String> IMAGE_EXTENSIONS =
            List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".ico");


    // 构造函数
    public FileDTO(String fileKey, Instant lastModified, Long sizeBytes) {
        this.fileKey = fileKey;
        this.fileName = extractFileName(fileKey); // 提取文件名
        this.lastModified = lastModified;
        this.sizeBytes = sizeBytes;

        // 调用新方法进行类型判断
        this.fileExtension = extractExtension(this.fileName).toLowerCase();
        this.isImage = IMAGE_EXTENSIONS.contains(this.fileExtension);
    }

    // 从 Key 中提取文件名 (假设 Key 是 'folder/subfolder/filename.ext')
    private String extractFileName(String key) {
        if (key == null || key.isEmpty()) {
            return "";
        }
        int lastSlash = key.lastIndexOf('/');
        return lastSlash >= 0 ? key.substring(lastSlash + 1) : key;
    }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Instant getLastModified() { return lastModified; }
    public void setLastModified(Instant lastModified) { this.lastModified = lastModified; }

    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getTempImageUrl() { return tempImageUrl; }
    public void setTempImageUrl(String tempImageUrl) { this.tempImageUrl = tempImageUrl; }


    private String extractExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(lastDot) : "";
    }

    // === Getters and Setters (新增部分) ===

    public boolean isImage() { return isImage; }
    public void setImage(boolean image) { isImage = image; }

    public String getFileExtension() { return fileExtension; }
    public void setFileExtension(String fileExtension) { this.fileExtension = fileExtension; }
}
