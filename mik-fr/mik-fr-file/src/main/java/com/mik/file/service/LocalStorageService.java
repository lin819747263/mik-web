package com.mik.file.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "file.type", havingValue = "local")
public class LocalStorageService {

    public void upload(String path,String finalName, MultipartFile file) throws IOException {
        //  保存的文件
        File savedFile = new File(path, finalName);

        if (!savedFile.getParentFile().exists()) {
            savedFile.getParentFile().mkdirs();
        }
        //转存文件
        file.transferTo(savedFile);
    }
}
