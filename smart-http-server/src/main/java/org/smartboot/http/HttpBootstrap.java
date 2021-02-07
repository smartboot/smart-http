/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpBootstrap.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.http.common.HttpServerHandle;
import org.smartboot.http.common.Pipeline;
import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.http.server.Request;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;
import java.util.function.Function;

public class HttpBootstrap {

    private static final String BANNER = "                               _       _      _    _          \n" +
            "                              ( )_    ( )    ( )_ ( )_        \n" +
            "  ___   ___ ___     _ _  _ __ | ,_)   | |__  | ,_)| ,_) _ _   \n" +
            "/',__)/' _ ` _ `\\ /'_` )( '__)| |     |  _ `\\| |  | |  ( '_`\\ \n" +
            "\\__, \\| ( ) ( ) |( (_| || |   | |_    | | | || |_ | |_ | (_) )\n" +
            "(____/(_) (_) (_)`\\__,_)(_)   `\\__)   (_) (_)`\\__)`\\__)| ,__/'\n" +
            "                                                       | |    \n" +
            "                                                       (_)   ";

    private static final String VERSION = "1.0.22";
    /**
     * http消息解码器
     */
    private final HttpRequestProtocol protocol = new HttpRequestProtocol();
    private final HttpMessageProcessor processor = new HttpMessageProcessor();
    private Function<HttpMessageProcessor, MessageProcessor<Request>> processorFunction = new Function<HttpMessageProcessor, MessageProcessor<Request>>() {
        @Override
        public MessageProcessor<Request> apply(HttpMessageProcessor httpMessageProcessor) {
            return httpMessageProcessor;
        }
    };
    private AioQuickServer<Request> server;
    /**
     * Http服务端口号
     */
    private int port = 8080;
    /**
     * read缓冲区大小
     */
    private int readBufferSize = 1024;
    /**
     * 服务线程数
     */
    private int threadNum = Runtime.getRuntime().availableProcessors() + 2;
    private int pageSize = 1024 * 1024;
    private int pageNum = threadNum;
    private int writeBufferSize = 1024;
    private String host;
    /**
     * 是否启用控制台banner
     */
    private boolean bannerEnabled = true;

    private int readPageSize = 1024 * 1024;

    /**
     * 设置HTTP服务端端口号
     *
     * @param port
     * @return
     */
    public HttpBootstrap setPort(int port) {
        this.port = port;
        return this;
    }

    public HttpBootstrap host(String host) {
        this.host = host;
        return this;
    }

    public Pipeline<HttpRequest, HttpResponse> pipeline() {
        return processor.pipeline();
    }

    public HttpBootstrap pipeline(HttpServerHandle httpHandle) {
        pipeline().next(httpHandle);
        return this;
    }

    public Pipeline<WebSocketRequest, WebSocketResponse> wsPipeline() {
        return processor.wsPipeline();
    }

    /**
     * 设置read缓冲区大小
     *
     * @param readBufferSize
     * @return
     */
    public HttpBootstrap setReadBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    /**
     * 设置服务线程数
     *
     * @param threadNum
     * @return
     */
    public HttpBootstrap setThreadNum(int threadNum) {
        this.threadNum = threadNum;
        return this;
    }

    public HttpBootstrap setBufferPool(int pageSize, int pageNum, int chunkSize) {
        this.pageSize = pageSize;
        this.pageNum = pageNum;
        this.writeBufferSize = chunkSize;
        return this;
    }

    /**
     * 启动HTTP服务
     */
    public void start() {
        BufferPagePool readBufferPool = new BufferPagePool(readPageSize, 1, false);
        server = new AioQuickServer<>(host, port, protocol, processorFunction.apply(processor));
        server.setThreadNum(threadNum)
                .setBannerEnabled(false)
                .setBufferFactory(() -> new BufferPagePool(pageSize, pageNum, true))
                .setReadBufferFactory(bufferPage -> readBufferPool.allocateBufferPage().allocate(readBufferSize))
                .setWriteBuffer(writeBufferSize, 16);
        try {
            if (bannerEnabled) {
                System.out.println(BANNER + "\r\n :: smart-http :: (" + VERSION + ")");
            }
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpBootstrap setBannerEnabled(boolean bannerEnabled) {
        this.bannerEnabled = bannerEnabled;
        return this;
    }

    public HttpBootstrap wrapProcessor(Function<HttpMessageProcessor, MessageProcessor<Request>> processorFunction) {
        this.processorFunction = processorFunction;
        return this;
    }

    public HttpBootstrap setReadPageSize(int readPageSize) {
        this.readPageSize = readPageSize;
        return this;
    }

    /**
     * 停止服务
     */
    public void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}
