package com.yxw.expression.resolve.controller;

import com.yxw.expression.resolve.dto.FileDTO;
import com.yxw.expression.resolve.dto.S3Item;
import com.yxw.expression.resolve.service.S3StorageService;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class FileViewController {

    private final S3StorageService s3StorageService;

    public FileViewController(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    /**
     * 渲染 S3 图片列表页面
     */
    @GetMapping("/s3/images")
    public String viewS3Images(Model model) {
        try {
            // 获取包含临时 URL 的文件列表
            List<FileDTO> imageList = s3StorageService.getFileListWithPresignedUrls();

            // 将数据添加到模型中
            model.addAttribute("imageList", imageList);

            // 返回模板名称 (对应 resources/templates/s3-list.html)
            return "s3-list";

        } catch (Exception e) {
            // 简单错误处理
            model.addAttribute("error", "无法加载图片列表：" + e.getMessage());
            return "error-page";
        }
    }

    /**
     * 渲染 S3 图片列表页面
     */
    @GetMapping("/s3/upload")
    public String s3Upload() {
        return "s3-upload";
    }



    // 首页列表 API
    @GetMapping("/s3/list")
    public String listFiles(@RequestParam(value = "prefix", required = false, defaultValue = "") String prefix, Model model) {
        // 调用 S3Service 获取列表
        List<S3Item> items = s3StorageService.listItems(prefix);

        model.addAttribute("items", items);
        model.addAttribute("currentPrefix", prefix);

        // 传递路径面包屑，方便前端渲染
        model.addAttribute("breadcrumbs", createBreadcrumbs(prefix));

        String parentPrefix = getParentPrefix(prefix);
        model.addAttribute("parentPrefix", parentPrefix);

        return "s3-list2";
    }

    // 辅助方法：生成面包屑导航数据
    private List<Breadcrumb> createBreadcrumbs(String prefix) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        // 根目录
        breadcrumbs.add(new Breadcrumb("Home", "/"));

        if (prefix == null || prefix.isEmpty()) {
            return breadcrumbs;
        }

        String currentPath = "";
        // 清理并分割路径
        String[] parts = prefix.split("/");

        for (String part : parts) {
            if (!part.isEmpty()) {
                currentPath += part + "/";
                // URL 路径需要编码，但 Thymeleaf 会处理
                breadcrumbs.add(new Breadcrumb(part, "/?prefix=" + currentPath));
            }
        }
        return breadcrumbs;
    }

    @Data
    // 辅助类：面包屑结构
    public static class Breadcrumb {
        private String name;
        private String url;
        // Getter/Setter...
        public Breadcrumb(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }


    // 辅助方法：计算上一级目录的 Prefix
    private String getParentPrefix(String currentPrefix) {
        if (currentPrefix == null || currentPrefix.isEmpty()) {
            return ""; // 已经是根目录，上一级也是根目录
        }

        // 确保路径以 '/' 结尾 (例如：folder1/folder2/)
        String path = currentPrefix;
        if (!path.endsWith("/")) {
            path += "/";
        }

        // 移除末尾的 '/'
        String trimmed = path.substring(0, path.length() - 1);

        // 找到倒数第二个 '/' 的位置
        int lastSeparatorIndex = trimmed.lastIndexOf('/');

        if (lastSeparatorIndex < 0) {
            // 如果只剩下一个顶级文件夹 (例如 "folder1")，则上一级是根目录
            return "";
        }

        // 截取到倒数第二个 '/' 的位置，并确保以 '/' 结尾
        return trimmed.substring(0, lastSeparatorIndex + 1);
    }
}