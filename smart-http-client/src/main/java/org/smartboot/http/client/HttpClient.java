/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpClient.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.extension.plugins.Plugin;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.factory.ClientSSLContextFactory;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.Base64;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public final class HttpClient {

    private final HttpClientConfiguration configuration;

    /**
     * Header: Host
     */
    private final String hostHeader;
    /**
     * 客户端Client
     */
    private AioQuickClient client;

    /**
     * 绑定线程池资源组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    private boolean connected;

    private boolean firstConnected = true;

    /**
     * Http 解码协议
     */
    private final HttpResponseProtocol protocol = HttpResponseProtocol.INSTANCE;
    /**
     * 消息处理器
     */
    private final HttpMessageProcessor processor = new HttpMessageProcessor();
    private final ConcurrentLinkedQueue<AbstractResponse> queue = new ConcurrentLinkedQueue<>();
    private final String uri;

    public HttpClient(String url) {
        int schemaIndex = url.indexOf("://");
        if (schemaIndex == -1) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        String schema = url.substring(0, schemaIndex);
        int uriIndex = url.indexOf("/", schemaIndex + 3);
        int portIndex = url.indexOf(":", schemaIndex + 3);
        boolean http = Constant.SCHEMA_HTTP.equals(schema);
        boolean https = !http && Constant.SCHEMA_HTTPS.equals(schema);

        if (!http && !https) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        String host;
        int port;
        if (portIndex > 0) {
            host = url.substring(schemaIndex + 3, portIndex);
            port = NumberUtils.toInt(uriIndex > 0 ? url.substring(portIndex + 1, uriIndex) : url.substring(portIndex + 1), -1);
        } else if (uriIndex > 0) {
            host = url.substring(schemaIndex + 3, uriIndex);
            port = https ? 443 : 80;
        } else {
            host = url.substring(schemaIndex + 3);
            port = https ? 443 : 80;
        }
        if (port == -1) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        this.configuration = new HttpClientConfiguration(host, port);
        configuration.setHttps(https);
        hostHeader = configuration.getHost() + ":" + configuration.getPort();
        this.uri = uriIndex > 0 ? url.substring(uriIndex) : "/";
    }

    public HttpClient(String host, int port) {
        this.configuration = new HttpClientConfiguration(host, port);
        hostHeader = configuration.getHost() + ":" + configuration.getPort();
        this.uri = null;
    }

    public HttpGet get() {
        connect();
        HttpGet httpGet = new HttpGet(client.getSession(), queue);
        initRest(httpGet, uri);
        return httpGet;
    }

    public HttpGet get(String uri) {
        connect();
        HttpGet httpGet = new HttpGet(client.getSession(), queue);
        initRest(httpGet, uri);
        return httpGet;
    }

    public HttpRest rest(String uri) {
        connect();
        HttpRest httpRest = new HttpRest(client.getSession(), queue);
        initRest(httpRest, uri);
        return httpRest;
    }

    public HttpPost post(String uri) {
        connect();
        HttpPost httpRest = new HttpPost(client.getSession(), queue);
        initRest(httpRest, uri);
        return httpRest;
    }

    public HttpPost post() {
        if (uri == null) {
            throw new UnsupportedOperationException("this method only support on constructor: HttpClient(String url)");
        }
        return post(uri);
    }

    private void initRest(HttpRest httpRest, String uri) {
        if (configuration.getProxy() != null && StringUtils.isNotBlank(configuration.getProxy().getProxyUserName())) {
            httpRest.request.addHeader(HeaderNameEnum.PROXY_AUTHORIZATION.getName(), "Basic " + Base64.getEncoder().encodeToString((configuration.getProxy().getProxyUserName() + ":" + configuration.getProxy().getProxyPassword()).getBytes()));
        }
        httpRest.request.setUri(uri);
        httpRest.request.addHeader(HeaderNameEnum.HOST.getName(), hostHeader);
        httpRest.request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());

        httpRest.completableFuture.thenAccept(httpResponse -> {
            AioSession session = client.getSession();
            ResponseAttachment attachment = session.getAttachment();
            //重置附件，为下一个响应作准备
            synchronized (session) {
                attachment.setDecoder(null);
                attachment.setResponse(queue.poll());
            }
            //request标注为keep-alive，response不包含该header,默认保持连接.
            if (HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpRest.request.getHeader(HeaderNameEnum.CONNECTION.getName())) && httpResponse.getHeader(HeaderNameEnum.CONNECTION.getName()) == null) {
                return;
            }
            //存在链路复用情况
            if (attachment.getResponse() != null || !queue.isEmpty()) {
                return;
            }
            //非keep-alive,主动断开连接
            if (!HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpResponse.getHeader(HeaderNameEnum.CONNECTION.getName()))) {
                close();
            } else if (!HeaderValueEnum.KEEPALIVE.getName().equalsIgnoreCase(httpRest.request.getHeader(HeaderNameEnum.CONNECTION.getName()))) {
                close();
            }
        });
        httpRest.completableFuture.exceptionally(throwable -> {
            close();
            return null;
        });
    }


    public HttpClientConfiguration configuration() {
        return configuration;
    }

    private void connect() {
        if (connected) {
            AioSession session = client.getSession();
            if (session == null || session.isInvalid()) {
                close();
                connect();
            }
            return;
        }

        try {
            if (firstConnected) {
                boolean noneSslPlugin = true;
                for (Plugin responsePlugin : configuration.getPlugins()) {
                    processor.addPlugin(responsePlugin);
                    if (responsePlugin instanceof SslPlugin) {
                        noneSslPlugin = false;
                    }
                }
                if (noneSslPlugin && configuration.isHttps()) {
                    processor.addPlugin(new SslPlugin<>(new ClientSSLContextFactory()));
                }

                firstConnected = false;
            }
            connected = true;
            client = configuration.getProxy() == null ? new AioQuickClient(configuration.getHost(), configuration.getPort(), protocol, processor) : new AioQuickClient(configuration.getProxy().getProxyHost(), configuration.getProxy().getProxyPort(), protocol, processor);
            BufferPagePool readPool = configuration.getReadBufferPool();
            client.setBufferPagePool(configuration.getWriteBufferPool()).setReadBufferFactory(bufferPage -> readPool == null ? VirtualBuffer.wrap(ByteBuffer.allocate(configuration.readBufferSize())) : readPool.allocateBufferPage().allocate(configuration.readBufferSize()));
            if (configuration.getConnectTimeout() > 0) {
                client.connectTimeout(configuration.getConnectTimeout());
            }
            if (asynchronousChannelGroup == null) {
                client.start();
            } else {
                client.start(asynchronousChannelGroup);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    public void close() {
        connected = false;
        client.shutdownNow();
    }

}
