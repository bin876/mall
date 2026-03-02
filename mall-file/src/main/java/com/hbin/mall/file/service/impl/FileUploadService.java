package com.hbin.mall.file.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private static final Set<String> ALLOWED_BIZ_TYPES =
            Set.of("brand", "spu", "sku", "avatar", "category", "banner");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private static final Map<String, byte[][]> MAGIC_NUMBERS = Map.of(
            "image/jpeg", new byte[][] {{(byte)0xFF, (byte)0xD8, (byte)0xFF}},
            "image/png", new byte[][] {{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}},
            "image/gif", new byte[][] {{0x47, 0x49, 0x46, 0x38}}, // "GIF8"
            "image/webp", new byte[][] {{0x52, 0x49, 0x46, 0x46}}  // "RIFF"
    );

    public String uploadFile(MultipartFile file, String bizType) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        String ext = getFileExtension(file.getOriginalFilename());
        String objectName = String.format(
                "%s/%d_%s%s",
                bizType,
                System.currentTimeMillis(),
                UUID.randomUUID().toString().substring(0, 8),
                ext
        );

        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        return objectName;
    }

    public String getPublicUrl(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            throw new IllegalArgumentException("Object name must not be null or empty");
        }
        String normalized = objectName.startsWith("/") ? objectName.substring(1) : objectName;
        String encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8);
        return String.format("%s/%s/%s",
                System.getProperty("minio.public-endpoint", endpoint),
                bucketName,
                encoded
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    public String upload(MultipartFile file, String bizType) throws Exception {
        if (!ALLOWED_BIZ_TYPES.contains(bizType)) {
            throw new IllegalArgumentException("非法业务类型: " + bizType);
        }

        validateFile(file);

        String objectName = this.uploadFile(file, bizType);
        return this.getPublicUrl(objectName);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        String contentType = file.getContentType();

        byte[][] magicPatterns = MAGIC_NUMBERS.get(contentType);
        if (magicPatterns == null) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("仅允许上传 JPG/PNG/GIF/WebP 图片");
        }

        String originalName = file.getOriginalFilename();
        if (originalName != null) {
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            if (!Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp").contains(ext)) {
                throw new IllegalArgumentException("不支持的图片格式");
            }
        }
        byte[] header = new byte[8];
        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.read(header);
            if (bytesRead < Math.min(4, header.length)) {
                throw new IllegalArgumentException("文件头信息不足");
            }
        }

        boolean matched = Arrays.stream(magicPatterns).anyMatch(pattern ->
                Arrays.equals(Arrays.copyOf(header, pattern.length), pattern)
        );

        if (!matched) {
            throw new IllegalArgumentException("文件内容与声明类型不匹配，疑似伪装文件");
        }
    }
}