package com.mik.file.controller;

import com.mik.core.pojo.Result;
import com.mik.file.FileOutput;
import com.mik.file.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping()
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("file/upload")
    public Result<FileOutput> upload(MultipartFile file, String path, Boolean rename) {
        return Result.success(fileService.upload(file, path, rename));
    }

    @PostMapping("v1/file/upload")
    public Result<FileOutput> uploadv1(MultipartFile file) {
        return Result.success(fileService.upload(file, "fix/", true));
    }

}
