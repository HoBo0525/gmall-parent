package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Hobo
 * @create 2021-02-02 20:09
 */
@Api(tags = "文件上传接口")
@RestController
@RequestMapping("admin/product")
public class FileUploadController {
    // fileUrl = http://192.168.200.128:8080/
    @Value("${fileServer.url}")
    private String fileUrl;

    @ApiOperation("文件上传")
    @PostMapping("fileUpload")
    public Result fileUpload(MultipartFile file){
          /*
            1.  读取到tracker.conf 文件
            2.  初始化FastDFS
            3.  创建对应的TrackerClient,TrackerServer
            4.  创建一个StorageClient，调用文件上传方法
            5.  获取到文件上传的url 并返回
         */
        //读取tracker.conf文件
        String confFile = this.getClass().getResource("/tracker.conf").getFile();



        String path = null;
        if (confFile != null){
            try {
                //初始化FastDFS
                ClientGlobal.init(confFile);

                //创建对应的TrackerClient,TrackerServer
                TrackerClient trackerClient = new TrackerClient();
                TrackerServer trackerServer = trackerClient.getConnection();

                //创建一个StorageClient，
                StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
                String extName = FilenameUtils.getExtension(file.getOriginalFilename());
                //调用文件上传方法
                //  path=group1/M00/00/02/wKjIgF_GZBqEKKqDAAAAAIyTSXk606.png
                path = storageClient1.upload_appender_file1(file.getBytes(), extName, null );

            } catch (IOException e) {
                e.printStackTrace();
            } catch (MyException e) {
                e.printStackTrace();
            }
        }

        //  将文件的整体全路径放入data 中
        //  http://192.168.200.128:8080/group1/M00/00/02/wKjIgF_GZBqEKKqDAAAAAIyTSXk606.png
        return Result.ok(fileUrl + path);
    }
}
