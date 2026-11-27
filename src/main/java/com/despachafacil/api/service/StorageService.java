package com.despachafacil.api.service;

import com.despachafacil.api.dto.StorageUploadResponse;
import com.despachafacil.api.security.TenantContext;
import com.despachafacil.api.security.TenantValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    private final TenantValidator tenantValidator;

    @Value("${cloudflare.r2.account-id:${CLOUDFLARE_R2_ACCOUNT_ID:}}")
    private String accountId;

    @Value("${cloudflare.r2.access-key-id:${CLOUDFLARE_R2_ACCESS_KEY_ID:}}")
    private String accessKeyId;

    @Value("${cloudflare.r2.secret-access-key:${CLOUDFLARE_R2_SECRET_ACCESS_KEY:}}")
    private String secretAccessKey;

    @Value("${cloudflare.r2.bucket-name:${CLOUDFLARE_R2_BUCKET_NAME:despacha-facil-storage}}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url:${CLOUDFLARE_R2_PUBLIC_URL:}}")
    private String publicUrl;

    public StorageService(TenantValidator tenantValidator) {
        this.tenantValidator = tenantValidator;
    }

    public StorageUploadResponse uploadFile(MultipartFile file, String folder) {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");
        }

        String sanitizedFolder = (folder != null && !folder.isBlank())
                ? folder.replaceAll("[^a-zA-Z0-9_-]", "")
                : "general";

        String extension = "webp";
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (ext.matches("^[a-z0-9]+$")) {
                extension = ext;
            }
        }

        String fileKey = String.format("tenants/%s/%s/%s.%s", tenantId, sanitizedFolder, UUID.randomUUID(), extension);
        String contentType = file.getContentType() != null ? file.getContentType() : "image/webp";

        // Si Cloudflare R2 está configurado con credenciales, subir a R2
        if (isR2Configured()) {
            return uploadToR2(file, fileKey, contentType, tenantId);
        }

        // Modo de desarrollo local: guardar en directorio local de subidas
        return uploadToLocalStorage(file, fileKey, contentType, tenantId);
    }

    private boolean isR2Configured() {
        return accountId != null && !accountId.isBlank()
                && accessKeyId != null && !accessKeyId.isBlank()
                && secretAccessKey != null && !secretAccessKey.isBlank();
    }

    private StorageUploadResponse uploadToR2(MultipartFile file, String fileKey, String contentType, UUID tenantId) {
        try {
            URI endpoint = URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId.trim()));
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId.trim(), secretAccessKey.trim());

            S3Configuration serviceConfiguration = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();

            try (S3Client s3Client = S3Client.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of("auto"))
                    .serviceConfiguration(serviceConfiguration)
                    .build()) {

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName.trim())
                        .key(fileKey)
                        .contentType(contentType)
                        .cacheControl("public, max-age=31536000, immutable")
                        .build();

                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                String baseUrl = (publicUrl != null && !publicUrl.isBlank())
                        ? publicUrl.trim().replaceAll("/+$", "")
                        : String.format("https://%s.r2.cloudflarestorage.com/%s", accountId.trim(), bucketName.trim());

                String finalUrl = String.format("%s/%s", baseUrl, fileKey);

                log.info("[STORAGE][UPLOAD_R2] [Tenant: {}] Archivo subido exitosamente a Cloudflare R2 [Key: {}] [Size: {} B]",
                        tenantId, fileKey, file.getSize());

                return new StorageUploadResponse(finalUrl, fileKey, file.getSize(), contentType);
            }
        } catch (Exception e) {
            log.error("[STORAGE][ERROR_R2] [Tenant: {}] Error al subir archivo a Cloudflare R2: {}", tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al almacenar el archivo en la nube");
        }
    }

    private StorageUploadResponse uploadToLocalStorage(MultipartFile file, String fileKey, String contentType, UUID tenantId) {
        try {
            Path targetPath = Paths.get("uploads", fileKey);
            Files.createDirectories(targetPath.getParent());

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String localUrl = "http://localhost:8080/uploads/" + fileKey;

            log.info("[STORAGE][UPLOAD_LOCAL] [Tenant: {}] R2 no configurado, archivo guardado localmente [Path: {}] [Size: {} B]",
                    tenantId, targetPath, file.getSize());

            return new StorageUploadResponse(localUrl, fileKey, file.getSize(), contentType);
        } catch (IOException e) {
            log.error("[STORAGE][ERROR_LOCAL] [Tenant: {}] Error al guardar archivo localmente: {}", tenantId, e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el archivo localmente");
        }
    }

    public void deleteFile(String fileUrlOrKey) {
        if (fileUrlOrKey == null || fileUrlOrKey.isBlank()) {
            return;
        }

        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        String tenantPrefix = "tenants/" + tenantId + "/";

        String key;
        if (fileUrlOrKey.contains(tenantPrefix)) {
            key = fileUrlOrKey.substring(fileUrlOrKey.indexOf(tenantPrefix));
        } else {
            log.debug("[STORAGE][DELETE_SKIP] La URL no es un recurso del tenant o es externa: {}", fileUrlOrKey);
            return;
        }

        if (isR2Configured()) {
            deleteFromR2(key, tenantId);
        } else {
            deleteFromLocalStorage(key, tenantId);
        }
    }

    private void deleteFromR2(String key, UUID tenantId) {
        try {
            URI endpoint = URI.create(String.format("https://%s.r2.cloudflarestorage.com", accountId.trim()));
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId.trim(), secretAccessKey.trim());
            S3Configuration serviceConfiguration = S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build();

            try (S3Client s3Client = S3Client.builder()
                    .endpointOverride(endpoint)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .region(Region.of("auto"))
                    .serviceConfiguration(serviceConfiguration)
                    .build()) {

                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName.trim())
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
                log.info("[STORAGE][DELETE_R2] [Tenant: {}] Objeto eliminado exitosamente de Cloudflare R2 [Key: {}]", tenantId, key);
            }
        } catch (Exception e) {
            log.error("[STORAGE][ERROR_DELETE_R2] [Tenant: {}] Error al eliminar archivo de Cloudflare R2 [Key: {}]: {}", tenantId, key, e.getMessage(), e);
        }
    }

    private void deleteFromLocalStorage(String key, UUID tenantId) {
        try {
            Path targetPath = Paths.get("uploads", key);
            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("[STORAGE][DELETE_LOCAL] [Tenant: {}] Archivo local eliminado [Path: {}]", tenantId, targetPath);
            }
        } catch (IOException e) {
            log.error("[STORAGE][ERROR_DELETE_LOCAL] [Tenant: {}] Error al eliminar archivo local [Key: {}]: {}", tenantId, key, e.getMessage());
        }
    }
}
