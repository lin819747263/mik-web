package com.mik.file.service;

import com.mik.core.exception.ServiceException;
import com.mik.file.FileOutput;
import com.mik.file.config.StaticResourceConfigure;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.ServerException;
import java.security.SecureRandom;
import java.util.Random;

@Configuration
@ConditionalOnProperty(name = "file.type", havingValue = "local")
public class LocalStorageService implements FileService{

    private static final Random SECURE_RANDOM;
    private static final int MD5_RANDOM_BOUND = 90;
    private static final int MD5_RANDOM_BOUND_TEN = 10;
    static {
        SECURE_RANDOM = new SecureRandom();
    }

    @Autowired
    StaticResourceConfigure staticResourceConfigure;

    @Override
    public FileOutput upload(MultipartFile file, String path, Boolean rename) {
        String absolutePath = staticResourceConfigure.getPath() + path;
        String finalName = rename(file.getOriginalFilename(), rename);
        //  保存的文件
        File savedFile = new File(absolutePath, finalName);

        if (!savedFile.getParentFile().exists()) {
            savedFile.getParentFile().mkdirs();
        }
        //转存文件
        try {
            file.transferTo(savedFile);
        }catch (IOException e){
            throw new ServiceException("文件上传失败");
        }
        String url = staticResourceConfigure.getUrl() + resove(path) + finalName;
        return new FileOutput().setUrl(url);
    }

    private String resove(String path) {
        if(path.startsWith("/")){
            return path.substring(1);
        }
        return path;
    }

    private static String rename(String originalName, Boolean rename) {
        if(!rename){
            return originalName;
        }
        String fileExtension = getFileExtension(originalName);
        // 获取文件的MD5值 做文件的唯一标识
        String md5Hex = DigestUtils.md5Hex(originalName);
        long time = System.currentTimeMillis();
        int number = SECURE_RANDOM.nextInt(MD5_RANDOM_BOUND) + MD5_RANDOM_BOUND_TEN;

        return DigestUtils.md5Hex(md5Hex + time + number) + "." + fileExtension;
    }

    public static String getFileExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        // 截取最后一个 "." 之后的部分
        return originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    }
}
