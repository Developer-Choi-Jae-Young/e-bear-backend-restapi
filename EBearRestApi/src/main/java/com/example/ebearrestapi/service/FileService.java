package com.example.ebearrestapi.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileService {
    public String uploadImage(MultipartFile file) throws IOException {
        String savePath = getSavePath();
        File folder = new File(savePath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String savedName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File target = new File(savePath + File.separator + savedName);
        file.transferTo(target);
        return  "http://localhost:8888/file/image/display/" + savedName;
    }

    public Resource displayImage(String fileName) {
        String savePath = getSavePath();
         return new FileSystemResource(savePath + File.separator + fileName);
    }

    public String getSavePath() {
        return System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" +
                File.separator + "resources" + File.separator + "static" +
                File.separator + "files";
    }
}
