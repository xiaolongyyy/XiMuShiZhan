package cn.itcast.core.controller;

import cn.itcast.common.utils.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("/uploadFile")
    public Result upload(MultipartFile file){

            String filename = file.getOriginalFilename();
        try {
            //创建一个FastDFS的客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fastDFS/fdfs_client.conf");
            //执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(),filename,file.getSize());
            //拼接返回制作再页面上回显
            String url=FILE_SERVER_URL+path;



           return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
           return new Result(false,"上传失败");
        }


    }
}
