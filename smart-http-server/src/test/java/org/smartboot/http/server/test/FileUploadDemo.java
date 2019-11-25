package org.smartboot.http.server.test;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.util.Streams;
import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.fileupload.SmartHttpFileUpload;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.handle.RouteHandle;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 三刀
 * @version V1.0 , 2019/11/24
 */
public class FileUploadDemo {
    public static void main(String[] args) {

        RouteHandle routeHandle = new RouteHandle();
        routeHandle.route("/", new HttpHandle() {
            byte[] body = ("<html>" +
                    "<head><title>smart-http demo</title></head>" +
                    "<body>" +
                    "GET 表单提交<form action='/get' method='get'><input type='text' name='text'/><input type='submit'/></form></br>" +
                    "POST 表单提交<form action='/post' method='post'><input type='text' name='text'/><input type='submit'/></form></br>" +
                    "文件上传<form action='/upload' method='post' enctype='multipart/form-data'><input type='file' name='text'/><input type='submit'/></form></br>" +
                    "</body></html>").getBytes();

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {

                response.setContentLength(body.length);
                response.getOutputStream().write(body);
            }
        })
                .route("/upload", new HttpHandle() {
                    @Override
                    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                        try {
                            SmartHttpFileUpload upload = new SmartHttpFileUpload();
                            FileItemIterator iterator = upload.getItemIterator(request);
                            while (iterator.hasNext()) {
                                FileItemStream item = iterator.next();
                                String name = item.getFieldName();
                                InputStream stream = item.openStream();
                                if (item.isFormField()) {
                                    System.out.println("Form field " + name + " with value "
                                            + Streams.asString(stream) + " detected.");
                                } else {
                                    System.out.println("File field " + name + " with file name "
                                            + item.getName() + " detected.");
                                    // Process the input stream
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


        HttpBootstrap bootstrap = new HttpBootstrap();
        //配置HTTP消息处理管道
        bootstrap.pipeline().next(routeHandle);

        //设定服务器配置并启动
        bootstrap.setPort(8080)
                .setReadBufferSize(4096)
                .setBufferPool(8 * 1024 * 1024, Runtime.getRuntime().availableProcessors() + 2, 4096)
                .start();
    }
}
