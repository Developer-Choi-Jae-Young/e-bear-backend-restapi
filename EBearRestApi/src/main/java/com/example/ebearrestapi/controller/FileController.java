package com.example.ebearrestapi.controller;

import com.example.ebearrestapi.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileService.uploadImage(file);
            return ResponseEntity.ok(Map.of("url", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/image/display/{fileName}")
    public ResponseEntity<?> displayImage(@PathVariable String fileName) {
        Resource resource = fileService.displayImage(fileName);
        if (!resource.exists()) return ResponseEntity.notFound().build();
        HttpHeaders headers = new HttpHeaders();

        try {
            String savePath = fileService.getSavePath();
            headers.add("Content-Type", Files.probeContentType(Paths.get(savePath + File.separator + fileName)));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
