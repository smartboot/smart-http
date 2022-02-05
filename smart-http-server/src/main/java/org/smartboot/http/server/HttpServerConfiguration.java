/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpServerConfiguration.java
 * Date: 2021-02-22
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.MessageProcessor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/22
 */
public class HttpServerConfiguration {

    /**
     * 缓存
     */
    private final ByteTree byteCache = new ByteTree();
    /**
     * URI缓存
     */
    private final ByteTree<ServerHandler> uriByteTree = new ByteTree<>();

    private final ByteTree<Function<String, ServerHandler>> headerNameByteTree = new ByteTree<>();

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
    private int threadNum = Math.max(Runtime.getRuntime().availableProcessors(), 2);
    private int writePageSize = 1024 * 1024;
    private int writePageNum = threadNum;
    private String host;
    private int readPageSize = 1024 * 1024;
    /**
     * 解析的header数量上限
     */
    private int headerLimiter = 0;
    /**
     * 启用 debug 模式后会打印码流
     */
    private boolean debug;
    /**
     * 服务器名称
     */
    private String serverName = "smart-http";
    private HttpServerHandler httpServerHandler = new HttpServerHandler() {
        @Override
        public void handle(HttpRequest request, HttpResponse response) throws IOException {
            response.write("Hello smart-http".getBytes(StandardCharsets.UTF_8));
        }
    };
    private WebSocketHandler webSocketHandler;

    private Function<MessageProcessor<Request>, MessageProcessor<Request>> processor = messageProcessor -> messageProcessor;

    Function<MessageProcessor<Request>, MessageProcessor<Request>> getProcessor() {
        return processor;
    }

    public HttpServerConfiguration messageProcessor(Function<MessageProcessor<Request>, MessageProcessor<Request>> processor) {
        this.processor = processor;
        this.getHeaderNameByteTree().addNode(HeaderNameEnum.UPGRADE.getName(), upgrade -> {
            // WebSocket
            if (HeaderValueEnum.WEBSOCKET.getName().equals(upgrade)) {
                return webSocketHandler;
            }
            // HTTP/2.0
            else if (HeaderValueEnum.H2C.getName().equals(upgrade) || HeaderValueEnum.H2.getName().equals(upgrade)) {
                return new Http2ServerHandler() {
                    @Override
                    public void handle(HttpRequest request, HttpResponse response) throws IOException {
                        httpServerHandler.handle(request, response);
                    }

                    @Override
                    public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws IOException {
                        httpServerHandler.handle(request, response, completableFuture);
                    }
                };
            } else {
                return null;
            }
        });
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

    public int getHeaderLimiter() {
        return headerLimiter;
    }

    /**
     * 支持解析的Header上限,若客户端提交Header数超过该值，超过部分将被忽略
     *
     * @param headerLimiter
     */
    public HttpServerConfiguration headerLimiter(int headerLimiter) {
        this.headerLimiter = headerLimiter;
        return this;
    }

    boolean isDebug() {
        return debug;
    }

    public HttpServerConfiguration debug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public String serverName() {
        return serverName;
    }

    public HttpServerConfiguration serverName(String server) {
        if (server == null) {
            this.serverName = null;
        } else {
            this.serverName = StringUtils.trim(server).replaceAll("\r", "").replaceAll("\n", "");
        }
        return this;
    }

    public ByteTree<ServerHandler> getUriByteTree() {
        return uriByteTree;
    }

    public HttpServerHandler getHttpServerHandler() {
        return httpServerHandler;
    }

    public void setHttpServerHandler(HttpServerHandler httpServerHandler) {
        this.httpServerHandler = httpServerHandler;
    }

    public WebSocketHandler getWebSocketHandler() {
        return webSocketHandler;
    }

    public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    /**
     * 将字符串缓存至 ByteTree 中，在Http报文解析过程中将获得更好的性能表现。
     * 适用反馈包括： URL、HeaderName、HeaderValue
     */
    public ByteTree getByteCache() {
        return byteCache;
    }

    public ByteTree<Function<String, ServerHandler>> getHeaderNameByteTree() {
        return headerNameByteTree;
    }
}
