package com.yxw.expression.resolve.dto;

import lombok.Data;

@Data
public class S3Item {
    private String name;    // 文件名或文件夹名
    private String path;    // 完整路径（用于后续点击）
    private boolean isDirectory;
    private long size;
}
