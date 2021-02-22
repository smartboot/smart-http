/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerConfiguration.java
 * Date: 2021-02-22
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.MessageProcessor;

import java.util.function.Function;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/22
 */
public class HttpServerConfiguration {

    /**
     * 是否启用控制台banner
     */
    private boolean bannerEnabled = true;

    /**
     * read缓冲区大小
     */
    private int readBufferSize = 1024;
    /**
     * write缓冲区大小
     */
    private int writeBufferSize = 1024;

    /**
     * 服务线程数
     */
    private int threadNum = Runtime.getRuntime().availableProcessors();
    private int writePageSize = 1024 * 1024;
    private int writePageNum = threadNum;

    private String host;
    private int readPageSize = 1024 * 1024;

    private Function<MessageProcessor<Request>, MessageProcessor<Request>> processor = messageProcessor -> messageProcessor;

    Function<MessageProcessor<Request>, MessageProcessor<Request>> getProcessor() {
        return processor;
    }

    public HttpServerConfiguration messageProcessor(Function<MessageProcessor<Request>, MessageProcessor<Request>> processor) {
        this.processor = processor;
        return this;
    }

    public HttpServerConfiguration readMemoryPool(int totalBytes) {
        this.readPageSize = totalBytes;
        return this;
    }

    int getReadPageSize() {
        return readPageSize;
    }

    public HttpServerConfiguration writeMemoryPool(int totalBytes, int shards) {
        this.writePageSize = totalBytes / shards;
        this.writePageNum = shards;
        return this;
    }

    int getReadBufferSize() {
        return readBufferSize;
    }

    /**
     * 设置read缓冲区大小
     *
     * @param readBufferSize
     * @return
     */
    public HttpServerConfiguration readBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    int getThreadNum() {
        return threadNum;
    }

    public HttpServerConfiguration threadNum(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    int getWritePageSize() {
        return writePageSize;
    }

    int getWritePageNum() {
        return writePageNum;
    }

    int getWriteBufferSize() {
        return writeBufferSize;
    }

    public HttpServerConfiguration writeBufferSize(int writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
        return this;
    }

    String getHost() {
        return host;
    }

    public HttpServerConfiguration host(String host) {
        this.host = host;
        return this;
    }

    boolean isBannerEnabled() {
        return bannerEnabled;
    }

    public HttpServerConfiguration bannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
        return this;
    }
}
