/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpBootstrap.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;

public class HttpBootstrap {


    private AioQuickServer<? extends HttpRequest> server;

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

    private int chunkSize = 1024;
    private HttpMessageProcessor processor = new HttpMessageProcessor();
    /**
     * http消息解码器
     */
    private HttpRequestProtocol protocol = new HttpRequestProtocol();

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

    public Pipeline pipeline() {
        return processor.pipeline();
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
        this.chunkSize = chunkSize;
        return this;
    }
    /**
     * 只配置keyStore则使用单向认证
     *
     * @param keyStore
     * @param storePassword
     * @return
     */
//    public HttpBootstrap sslKeyStore(String keyStore, String storePassword) {
//        this.keyStore = keyStore;
//        this.storePassword = storePassword;
//        return this;
//    }

    /**
     * 配合sslKeyStore提供双向认证
     *
     * @param trust
     * @param password
     * @return
     */
//    public HttpBootstrap sslTrustStore(String trust, String password) {
//        this.trust = trust;
//        this.trustPassword = password;
//        return this;
//    }

    /**
     * 启动HTTP服务
     */
    public void start() {
        server = new AioQuickServer<>(port, protocol, processor);
        server.setReadBufferSize(readBufferSize)
                .setThreadNum(threadNum)
                .setBufferPoolPageSize(pageSize)
                .setBufferPoolPageNum(pageNum)
                .setBufferPoolChunkSize(chunkSize);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
