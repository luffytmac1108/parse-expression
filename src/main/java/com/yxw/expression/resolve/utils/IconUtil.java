package com.yxw.expression.resolve.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class IconUtil {

    // 映射文件扩展名到 Font Awesome 图标 class
    private static final Map<String, String> ICON_MAP;

    // 使用静态代码块初始化，以避免 Map.of() 的参数限制
    static {
        Map<String, String> tempMap = new HashMap<>();

        // 文档类型
        tempMap.put(".pdf", "fa-file-pdf");
        tempMap.put(".doc", "fa-file-word");
        tempMap.put(".docx", "fa-file-word");
        tempMap.put(".xls", "fa-file-excel");
        tempMap.put(".xlsx", "fa-file-excel");
        tempMap.put(".ppt", "fa-file-powerpoint");
        tempMap.put(".pptx", "fa-file-powerpoint");
        tempMap.put(".txt", "fa-file-lines");
        tempMap.put(".csv", "fa-file-csv");

        // 压缩/代码/其他
        tempMap.put(".zip", "fa-file-zipper");
        tempMap.put(".rar", "fa-file-zipper");
        tempMap.put(".7z", "fa-file-zipper");
        tempMap.put(".java", "fa-file-code");
        tempMap.put(".js", "fa-file-code");
        tempMap.put(".html", "fa-file-code");
        tempMap.put(".xml", "fa-file-code");

        // 视频/音频
        tempMap.put(".mp4", "fa-file-video");
        tempMap.put(".mov", "fa-file-video");
        tempMap.put(".mp3", "fa-file-audio");
        tempMap.put(".wav", "fa-file-audio");

        // 将可变 Map 转换为不可变 Map
        ICON_MAP = Collections.unmodifiableMap(tempMap);
    }

    // 默认图标
    private static final String DEFAULT_ICON = "fa-file";

    /**
     * 根据文件扩展名获取 Font Awesome 图标的 class 名称。
     * @param extension 文件扩展名 (例如: .pdf, .docx)
     * @return 对应的 Font Awesome class (例如: fa-file-pdf)
     */
    public static String getFileIconClass(String extension) {
        String lowerCaseExt = extension != null ? extension.toLowerCase() : "";

        // 使用 getOrDefault，如果没有找到特定图标，则返回通用文件图标
        // 注意：这里使用 Map.getOrDefault(key, defaultValue)
        return ICON_MAP.getOrDefault(lowerCaseExt, DEFAULT_ICON);
    }
}
