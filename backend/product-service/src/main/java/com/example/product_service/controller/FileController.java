package com.example.product_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/api/v1/products/images")
public class FileController {

    private final Path fileStorageLocation;

    // Khai báo Logger thủ công
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    public FileController(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) { // Kiểm tra cả exists() và isReadable()
                String contentType = determineContentType(filename);
                log.info("Serving file: {}", filename); // Thêm log để theo dõi

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                log.warn("File not found or not readable: {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException ex) {
            log.error("Invalid file path for: {}", filename, ex);
            return ResponseEntity.badRequest().build();
        }
    }

    private String determineContentType(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        if (lowerCaseFilename.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        } else if (lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        } else if (lowerCaseFilename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF_VALUE;
        } else {
            log.warn("Unknown file type for: {}, defaulting to octet-stream", filename);
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }
}