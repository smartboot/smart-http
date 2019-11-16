/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpBootstrap.java
 * Date: 2018-01-28
 * Author: sandao
 */

package org.smartboot.http;

import org.smartboot.http.server.HttpMessageProcessor;
import org.smartboot.http.server.HttpRequestProtocol;
import org.smartboot.socket.extension.ssl.ClientAuth;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSSLQuickServer;

import java.io.IOException;

public class HttpBootstrap {


    private AioQuickServer<? extends HttpRequest> server;

    private AioSSLQuickServer<? extends HttpRequest> sslServer;
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
    private HttpMessageProcessor processor = new HttpMessageProcessor();
    /**
     * http消息解码器
     */
    private HttpRequestProtocol protocol = new HttpRequestProtocol();

    private String keyStore;

    private String trust;

    private String trustPassword;

    private int sslPort;

    private boolean sslEnabled;

    private String storePassword;

    private String keyPassword;

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
        server.setReadBufferSize(readBufferSize);
        server.setThreadNum(threadNum);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sslEnabled) {
            startSsl();
        }
    }

    private void startSsl() {
        // 定义服务器接受的消息类型以及各类消息对应的处理器
        sslServer = new AioSSLQuickServer<>(sslPort, protocol, processor);
        sslServer
                .setClientAuth(ClientAuth.OPTIONAL)
                .setKeyStore(ClassLoader.getSystemClassLoader().getResource(keyStore).getFile(), storePassword)
                .setTrust(ClassLoader.getSystemClassLoader().getResource(trust).getFile(), storePassword)
                .setKeyPassword(keyPassword);
        try {
            sslServer.start();
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
        if (sslServer != null) {
            sslServer.shutdown();
            sslServer = null;
        }
    }
}
