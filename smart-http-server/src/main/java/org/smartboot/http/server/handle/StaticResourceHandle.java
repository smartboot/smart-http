/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: StaticResourceRoute.java
 * Date: 2018-02-07
 * Author: sandao
 */

package org.smartboot.http.server.handle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.Mimetypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 静态资源加载Handle
 *
 * @author 三刀
 * @version V1.0 , 2018/2/7
 */
public class StaticResourceHandle extends HttpHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticResourceHandle.class);
    private static final int READ_BUFFER = 1024;
    private File baseDir;

    public StaticResourceHandle(String baseDir) {
        this.baseDir = new File(baseDir);
        if (!this.baseDir.isDirectory()) {
            throw new RuntimeException(baseDir + " is not a directory");
        }
        LOGGER.info("dir is:{}", this.baseDir.getAbsolutePath());
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        File file = new File(baseDir, request.getRequestURI());
        if (!file.isFile()) {
            LOGGER.warn("file:{} not found!", request.getRequestURI());
            response.setHttpStatus(HttpStatus.NOT_FOUND);
            return;
        }
        String contentType = Mimetypes.getInstance().getMimetype(file);
        response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, contentType);
        FileInputStream fis = new FileInputStream(file);
        FileChannel fileChannel = fis.getChannel();
        long fileSize = fileChannel.size();
        long readPos = 0;
        while (readPos < fileSize) {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, readPos, fileSize - readPos > READ_BUFFER ? READ_BUFFER : fileSize - readPos);
            readPos += mappedByteBuffer.remaining();
            response.write(mappedByteBuffer);
        }
        fis.close();
    }
}
