/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpBootstrap.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.aio.EnhanceAsynchronousChannelProvider;
import org.smartboot.http.server.impl.HttpMessageProcessor;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;

public class HttpBootstrap {

    private static final String BANNER = "                               _       _      _    _          \n" +
            "                              ( )_    ( )    ( )_ ( )_        \n" +
            "  ___   ___ ___     _ _  _ __ | ,_)   | |__  | ,_)| ,_) _ _   \n" +
            "/',__)/' _ ` _ `\\ /'_` )( '__)| |     |  _ `\\| |  | |  ( '_`\\ \n" +
            "\\__, \\| ( ) ( ) |( (_| || |   | |_    | | | || |_ | |_ | (_) )\n" +
            "(____/(_) (_) (_)`\\__,_)(_)   `\\__)   (_) (_)`\\__)`\\__)| ,__/'\n" +
            "                                                       | |    \n" +
            "                                                       (_)   ";

    private static final String VERSION = "1.1.7-SNAPSHOT";
    /**
     * http消息解码器
     */
    private final HttpMessageProcessor processor;
    private final HttpServerConfiguration configuration = new HttpServerConfiguration();
    private AioQuickServer server;
    /**
     * Http服务端口号
     */
    private int port = 8080;

    public HttpBootstrap() {
        this(new HttpMessageProcessor());
    }

    public HttpBootstrap(HttpMessageProcessor processor) {
        this.processor = processor;
    }

    /**
     * Http服务端口号
     */
    public HttpBootstrap setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * 往 http 处理器管道中注册 Handle
     *
     * @param httpHandler
     * @return
     */
    public HttpBootstrap httpHandler(HttpServerHandler httpHandler) {
        processor.httpServerHandler(httpHandler);
        return this;
    }

    /**
     * 获取websocket的处理器管道
     *
     * @return
     */
    public HttpBootstrap webSocketHandler(WebSocketHandler webSocketHandler) {
        processor.setWebSocketHandler(webSocketHandler);
        return this;
    }

    /**
     * 服务配置
     *
     * @return
     */
    public HttpServerConfiguration configuration() {
        return configuration;
    }

    /**
     * 启动HTTP服务
     */
    public void start() {
        System.setProperty("java.nio.channels.spi.AsynchronousChannelProvider", EnhanceAsynchronousChannelProvider.class.getName());
        BufferPagePool readBufferPool = new BufferPagePool(configuration.getReadPageSize(), 1, false);
        server = new AioQuickServer(configuration.getHost(), port, new HttpRequestProtocol(configuration), configuration.getProcessor().apply(processor));
        server.setThreadNum(configuration.getThreadNum())
                .setBannerEnabled(false)
                .setBufferFactory(() -> new BufferPagePool(configuration.getWritePageSize(), configuration.getWritePageNum(), true))
                .setReadBufferFactory(bufferPage -> readBufferPool.allocateBufferPage().allocate(configuration.getReadBufferSize()))
                .setWriteBuffer(configuration.getWriteBufferSize(), 16);
        try {
            if (configuration.isBannerEnabled()) {
                System.out.println(BANNER + "\r\n :: smart-http :: (" + VERSION + ")");
            }
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
