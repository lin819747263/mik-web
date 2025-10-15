package com.mik.file.service;

import com.mik.file.FileOutput;
import org.springframework.web.multipart.MultipartFile;


public interface FileService {

    FileOutput upload(MultipartFile file, String path, Boolean rename);

}
