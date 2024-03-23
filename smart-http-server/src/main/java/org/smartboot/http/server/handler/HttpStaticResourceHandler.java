/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: StaticResourceHandle.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.handler;

import org.smartboot.http.common.BufferOutputStream;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
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
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 静态资源加载Handle
 *
 * @author 三刀
 * @version V1.0 , 2018/2/7
 */
public class HttpStaticResourceHandler extends HttpServerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStaticResourceHandler.class);

    private final File baseDir;

    public HttpStaticResourceHandler(String baseDir) {
        this.baseDir = new File(new File(baseDir).getAbsolutePath());
        if (!this.baseDir.isDirectory()) {
            throw new RuntimeException(baseDir + " is not a directory");
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("dir is:{}", this.baseDir.getAbsolutePath());
        }
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws IOException {
        String fileName = request.getRequestURI();
        String method = request.getMethod();
        if (StringUtils.endsWith(fileName, "/")) {
            fileName += "index.html";
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("请求URL: " + fileName);
        }
        File file = new File(baseDir, URLDecoder.decode(fileName, StandardCharsets.UTF_8.name()));
        //404
        if (!file.isFile()) {
            LOGGER.warn("file: {} not found!", request.getRequestURI());
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), "text/html; charset=utf-8");

            if (!HttpMethodEnum.HEAD.getMethod().equals(method)) {
                throw new HttpException(HttpStatus.NOT_FOUND);
            }
            completableFuture.complete(null);
            return;
        }
        //304
        Date lastModifyDate = new Date(file.lastModified() / 1000 * 1000);
        try {
            String requestModified = request.getHeader(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
            if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= DateUtils.parseLastModified(requestModified).getTime()) {
                response.setHttpStatus(HttpStatus.NOT_MODIFIED);
                completableFuture.complete(null);
                return;
            }
        } catch (Exception e) {
            LOGGER.error("exception", e);
        }

        response.setHeader(HeaderNameEnum.LAST_MODIFIED.getName(), DateUtils.formatLastModified(lastModifyDate));
        response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), Mimetypes.getInstance().getMimetype(file) + "; charset=utf-8");
        //HEAD不输出内容
        if (HttpMethodEnum.HEAD.getMethod().equals(method)) {
            completableFuture.complete(null);
            return;
        }

        response.setContentLength((int) file.length());

        FileInputStream fis = new FileInputStream(file);
        completableFuture.whenComplete((o, throwable) -> {
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        long fileSize = response.getContentLength();
        AtomicLong readPos = new AtomicLong(0);
        byte[] bytes = new byte[1024 * 1024];
        int len;
        len = fis.read(bytes);
        if (len == -1) {
            completableFuture.completeExceptionally(new IOException("EOF"));
        } else if (readPos.addAndGet(len) >= fileSize) {
            response.getOutputStream().write(bytes, 0, len);
            completableFuture.complete(null);
        } else {
            response.getOutputStream().write(bytes, 0, len, new Consumer<BufferOutputStream>() {
                @Override
                public void accept(BufferOutputStream result) {
                    try {
                        int len = fis.read(bytes);
                        if (len == -1) {
                            completableFuture.completeExceptionally(new IOException("EOF"));
                        } else if (readPos.addAndGet(len) >= fileSize) {
                            response.getOutputStream().write(bytes, 0, len);
                            completableFuture.complete(null);
                        } else {
                            response.getOutputStream().write(bytes, 0, len, this);
                        }
                    } catch (Throwable throwable) {
                        completableFuture.completeExceptionally(throwable);
                    }
                }
            });
        }
    }
}
