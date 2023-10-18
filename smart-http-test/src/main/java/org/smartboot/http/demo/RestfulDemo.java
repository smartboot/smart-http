package org.smartboot.http.demo;

import org.smartboot.http.restful.RestfulBootstrap;
import org.smartboot.http.restful.annotation.Controller;
import org.smartboot.http.restful.annotation.RequestMapping;
import org.smartboot.http.restful.annotation.RequestMethod;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/1/27
 */
@Controller
public class RestfulDemo {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String helloworld() {
        return "hello world";
    }

    @RequestMapping("/")
    public String index() {
        return ("<html>" +
                "<head><title>smart-http demo</title></head>" +
                "<body>" +
                "GET 表单提交<form action='/get' method='get'><input type='text' name='text'/><input type='submit'/></form></br>" +
                "POST 表单提交<form action='/post' method='post'><input type='text' name='text'/><input type='submit'/></form></br>" +
                "文件上传<form action='/upload' method='post' enctype='multipart/form-data'>表单name:<input type='text' name='name'/> <input type='file' name='text'/><input type='submit'/></form></br>" +
                "</body></html>");
    }

//    @RequestMapping("/upload")
//    public String upload(@Param("text") MultipartFile file, @Param("name") String name) {
//        return "aa";
//    }

    public static void main(String[] args) throws Exception {
        RestfulBootstrap bootstrap = RestfulBootstrap.getInstance().controller(RestfulDemo.class);
        bootstrap.bootstrap().configuration().debug(true);
        bootstrap.bootstrap().setPort(8080).start();
    }
}
