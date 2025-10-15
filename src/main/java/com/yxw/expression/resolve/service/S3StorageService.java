package com.yxw.expression.resolve.service;

import com.yxw.expression.resolve.dto.FileDTO;
import com.yxw.expression.resolve.dto.S3Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3StorageService {

    private final S3Client s3Client;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * 上传文件到 S3，并返回一个有时效性的预签名 URL。
     * @param key 文件在 S3 中的唯一键 (路径/文件名)
     * @param inputStream 文件的输入流
     * @param contentLength 文件的大小
     * @return 文件的临时访问 URL (默认 5 分钟有效期)
     */
    public String uploadFileAndGetPresignedUrl(String key, InputStream inputStream, long contentLength) {
        // 1. 上传文件到 S3
        uploadFile(key, inputStream, contentLength);

        // 2. 生成临时访问 URL
        // 默认设置为 5 分钟有效期
        return generatePresignedUrl(key, Duration.ofMinutes(5));
    }

    // 内部方法：执行 S3 上传操作
    private void uploadFile(String key, InputStream inputStream, long contentLength) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    // 您可以在此添加 ContentType 等元数据
                    .build();

            // 执行上传
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));

        } catch (Exception e) {
            // 捕获 S3 客户端操作异常
            throw new RuntimeException("S3 客户端操作失败: " + key, e);
        }
    }

    // 内部方法：生成预签名 URL
    private String generatePresignedUrl(String key, Duration expiration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(expiration)
                .build();

        // 返回生成的 URL 字符串
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /**
     * 从 S3 下载文件并返回其字节数组。
     */
    public byte[] downloadFile(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            // 将 InputStream 转换为字节数组
            return StreamUtils.copyToByteArray(s3Object);
        } catch (Exception e) {
            throw new RuntimeException("S3 下载操作失败: " + key, e);
        }
    }

    /**
     * 直接从 S3 存储桶中获取文件列表，并为每个文件生成一个短期的预签名 URL。
     * @return 包含文件信息的 DTO 列表
     */
    public List<FileDTO> getFileListWithPresignedUrls() {

        // 1. 构建 ListObjects 请求
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        // 2. 执行 S3 API 调用，获取对象列表
        List<S3Object> s3Objects = s3Client.listObjectsV2(listRequest)
                .contents();

        // 3. 过滤、映射并生成 URL
        return s3Objects.stream()
                // 排除 S3 文件夹对象（它们通常以 '/' 结尾且 size=0）
                .filter(obj -> obj.size() > 0)
                .map(s3Object -> {
                    String fileKey = s3Object.key();

                    // 3a. 创建 DTO 对象
                    FileDTO dto = new FileDTO(
                            fileKey,
                            s3Object.lastModified(),
                            s3Object.size()
                    );

                    // 3b. 生成 1 分钟有效的预签名 URL
                    Duration expiration = Duration.ofMinutes(1);
                    String tempUrl = generatePresignedUrl(fileKey, expiration);

                    dto.setTempImageUrl(tempUrl);

                    return dto;
                })
                .collect(Collectors.toList());
    }




    public List<S3Item> listItems(String prefix) {
        // 确保 prefix 以 "/" 结尾，除非它是空字符串
        if (prefix != null && !prefix.isEmpty() && !prefix.endsWith("/")) {
            prefix = prefix + "/";
        }

        // 1. 构建 ListObjectsV2 请求
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)             // 当前路径作为前缀
                .delimiter("/")          // 核心：模拟文件夹分隔符
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        List<S3Item> items = new ArrayList<>();

        // 2. 处理 CommonPrefixes (即"文件夹")
        // 假设 response.commonPrefixes() 返回 List<CommonPrefixObject>
        // 并且 CommonPrefixObject 有一个 getPrefix() 方法
        for (CommonPrefix commonPrefixObject : response.commonPrefixes()) {
            String commonPrefix = commonPrefixObject.prefix();
            S3Item folder = new S3Item();
            // 文件夹名是前缀去掉当前路径后剩下的部分
            String folderName = commonPrefix.substring(prefix.length());
            folder.setName(folderName);
            folder.setPath(commonPrefix);
            folder.setDirectory(true);
            items.add(folder);
        }

        // 3. 处理 S3Objects (即"文件")
        // S3Object 的 Key 是完整路径
        for (S3Object s3Object : response.contents()) {
            // 忽略与当前 prefix 完全匹配的 Key（通常是空文件，或 S3 模拟文件夹的根对象）
            if (s3Object.key().equals(prefix)) {
                continue;
            }

            S3Item file = new S3Item();
            // 文件名是 Key 去掉当前路径后剩下的部分
            String fileName = s3Object.key().substring(prefix.length());

            file.setName(fileName);
            file.setPath(s3Object.key());
            file.setDirectory(false);
            file.setSize(s3Object.size());
            items.add(file);
        }

        return items;
    }
}
