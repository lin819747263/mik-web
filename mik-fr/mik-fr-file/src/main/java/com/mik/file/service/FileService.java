package com.mik.file.service;

import java.io.InputStream;

public interface FileService {

    String upload(String fileName, InputStream inputStream, Boolean rename);

    String batchUpload(String fileName, InputStream inputStream, Boolean rename);

}
