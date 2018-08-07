package com.agtuigu.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MyUploadUtil {

    public static String uploadImage(MultipartFile file) {
        // 配置fdfs的全局信息
        String fliePath = MyUploadUtil.class.getClassLoader().getResource("tracker.conf").getFile();
        TrackerServer connection = null;
        String[] images = new String[0];
        try {
            ClientGlobal.init(fliePath);
            // 获得tracker
            TrackerClient trackerClient = new TrackerClient();
            connection = trackerClient.getConnection();

            // 通过tracker获得storage
            StorageClient storageClient = new StorageClient(connection,null);

            // 通过storage上传文件

            // 通过最后一个"."获取扩展名
            String fileName = file.getOriginalFilename();
            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
            images = storageClient.upload_file(file.getBytes(), extName, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
        String url = "http://192.168.248.222";
        for (String image : images) {
            url = url + "/" + image;
        }
        return url;
    }
}
