package org.example.aispingboot.controller;

import org.example.aispingboot.common.Result;
import org.example.aispingboot.common.ResultCode;
import org.example.aispingboot.entity.FileInfo;
import org.example.aispingboot.mapper.FileInfoMapper;
import org.example.aispingboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传：保存到 {工作目录}/uploads/{businessType}/，返回可直接访问的 URL。
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) String businessType,
                                              @RequestParam(required = false) String businessId,
                                              @RequestParam(required = false) String businessField) {
        Long userId = JwtTokenUtil.getCurrentUserIdOrNull();
        if (userId == null) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        if (file == null || file.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "文件不能为空", null);
        }

        String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            ext = originalName.substring(dot).toLowerCase();
        }

        String type = (businessType == null || businessType.isBlank()) ? "COMMON" : businessType;
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Path dir = Paths.get(System.getProperty("user.dir"), "uploads", type);
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            file.transferTo(target.toFile());

            String url = "/uploads/" + type + "/" + fileName;

            FileInfo info = new FileInfo();
            info.setOriginalName(originalName);
            info.setFilePath(url);
            info.setFileSize(file.getSize());
            info.setFileType(ext.replace(".", ""));
            info.setBusinessType(type);
            info.setBusinessId(businessId);
            info.setBusinessField(businessField);
            info.setUploadUserId(userId);
            info.setIsTemp(1);
            info.setStatus(1);
            info.setCreateTime(LocalDateTime.now());
            fileInfoMapper.insert(info);

            Map<String, Object> data = new HashMap<>();
            data.put("id", info.getId());
            data.put("url", url);
            data.put("fileName", originalName);
            data.put("fileSize", file.getSize());
            return Result.ok(data);
        } catch (IOException e) {
            return Result.error(ResultCode.FILE_SAVE_FAILED.getCode(),
                    "文件保存失败：" + e.getMessage(), null);
        }
    }
}
