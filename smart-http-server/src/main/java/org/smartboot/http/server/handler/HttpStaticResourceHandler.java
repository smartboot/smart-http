/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: StaticResourceHandle.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handler;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.DateUtils;
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 静态资源加载Handle
 *
 * @author 三刀
 * @version V1.0 , 2018/2/7
 */
public class HttpStaticResourceHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStaticResourceHandler.class);
    private static final int READ_BUFFER = 1024 * 1024;
    private static final String URL_404 = "<html>" + "<head>" + "<title>smart-http 404</title>" + "</head>" + "<body><h1>smart-http 找不到你所请求的地址资源，404</h1></body>" + "</html>";

    private final File baseDir;

    private Map<String, FileChannel> channelMap = new ConcurrentHashMap<>();

    public HttpStaticResourceHandler(String baseDir) {
        this.baseDir = new File(new File(baseDir).getAbsolutePath());
        if (!this.baseDir.isDirectory()) {
            throw new RuntimeException(baseDir + " is not a directory");
        }
        if (LOGGER.isInfoEnabled()) LOGGER.info("dir is:{}", this.baseDir.getAbsolutePath());
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response) throws IOException {
        String fileName = request.getRequestURI();
        String method = request.getMethod();
        if (StringUtils.endsWith(fileName, "/")) {
            fileName += "index.html";
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("请求URL: " + fileName);
        }
        File file = new File(baseDir, URLDecoder.decode(fileName, StandardCharsets.UTF_8));
        //404
        if (!file.isFile()) {
            LOGGER.warn("file: {} not found!", request.getRequestURI());
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), "text/html; charset=utf-8");

            if (!HttpMethodEnum.HEAD.getMethod().equals(method)) {
                response.write(URL_404.getBytes());
            }
            return;
        }
        //304
        Date lastModifyDate = new Date(file.lastModified() / 1000 * 1000);
        try {
            String requestModified = request.getHeader(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
            if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= DateUtils.parseLastModified(requestModified).getTime()) {
                response.setHttpStatus(HttpStatus.NOT_MODIFIED);
                return;
            }
        } catch (Exception e) {
            LOGGER.error("exception", e);
        }
        response.setHeader(HeaderNameEnum.LAST_MODIFIED.getName(), DateUtils.formatLastModified(lastModifyDate));


        String contentType = Mimetypes.getInstance().getMimetype(file);
        response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), contentType + "; charset=utf-8");
        response.setContentLength((int) file.length());
        //HEAD不输出内容
        if (HttpMethodEnum.HEAD.getMethod().equals(method)) {
            return;
        }

        if (!file.getName().endsWith("html") && !file.getName().endsWith("htm")) {
            response.setContentLength((int) file.length());
        }


        FileChannel fileChannel = channelMap.computeIfAbsent(file.getName(), s -> {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return fis.getChannel();
        });
        long fileSize = response.getContentLength();
        long readPos = 0;
        while (readPos < fileSize) {
            long length = (fileSize - readPos) > READ_BUFFER ? READ_BUFFER : (fileSize - readPos);
            fileChannel.transferTo(readPos, length, response.getOutputStream());
//            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, readPos, fileSize - readPos > READ_BUFFER ? READ_BUFFER : fileSize - readPos);
            readPos += length;
//            byte[] data = new byte[mappedByteBuffer.remaining()];
//            mappedByteBuffer.get(data);
//            response.write(data);
        }
    }
}
