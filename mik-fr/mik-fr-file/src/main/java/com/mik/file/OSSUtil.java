package com.mik.file;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.mik.file.config.OssProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * OSS工具类
 */
public class OSSUtil {

    /**
     *
     * @Title: getOSSClient
     * @Description: 获取oss客户端
     * @return OSSClient oss客户端
     * @throws
     */
    public static OSS getOSSClient() {

        OssProperties properties = OssProperties.getInstance();
        OSS oss = new OSSClientBuilder().build(properties.getEndPoint(), properties.getAccessKey(), properties.getAccessSecret());
        return oss;
    }

    /**
     *
     * @Title: uploadByNetworkStream
     * @Description: 通过网络流上传文件
     * @param ossClient 	oss客户端
     * @param url 			URL
     * @param bucketName 	bucket名称
     * @param objectName 	上传文件目录和（包括文件名）例如“test/index.html”
     * @return void 		返回类型
     * @throws
     */
    public static void uploadByNetworkStream(OSS ossClient, URL url, String bucketName, String objectName) throws IOException {
        try {
            InputStream inputStream = url.openStream();
            ossClient.putObject(bucketName, objectName, inputStream);
            ossClient.shutdown();
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     *
     * @Title: uploadByInputStream
     * @Description: 通过输入流上传文件
     * @param ossClient 	oss客户端
     * @param inputStream 	输入流
     * @param bucketName 	bucket名称
     * @param objectName 	上传文件目录和（包括文件名） 例如“test/a.jpg”
     * @return void 		返回类型
     * @throws
     */
    public static void uploadByInputStream(OSS ossClient, InputStream inputStream, String bucketName,
                                           String objectName) {
        try {
            ossClient.putObject(bucketName, objectName, inputStream);
        }catch (Exception e){
            throw new RuntimeException("文件上传错误");
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     *
     * @Title: uploadByFile
     * @Description: 通过file上传文件
     * @param ossClient 	oss客户端
     * @param file 			上传的文件
     * @param bucketName 	bucket名称
     * @param objectName 	上传文件目录和（包括文件名） 例如“test/a.jpg”
     * @return void 		返回类型
     * @throws
     */
    public static void uploadByFile(OSS ossClient, File file, String bucketName, String objectName) {
        try {
            ossClient.putObject(bucketName, objectName, file);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     *
     * @Title: deleteFile
     * @Description: 根据key删除oss服务器上的文件
     * @param ossClient		oss客户端
     * @param bucketName		bucket名称
     * @param key    		文件路径/名称，例如“test/a.txt”
     * @return void    		返回类型
     * @throws
     */
    public static void deleteFile(OSS ossClient, String bucketName, String key) {
        ossClient.deleteObject(bucketName, key);
    }
}



