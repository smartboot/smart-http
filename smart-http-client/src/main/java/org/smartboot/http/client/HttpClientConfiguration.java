package org.smartboot.http.client;

import org.smartboot.http.client.impl.Response;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.extension.plugins.Plugin;
import org.smartboot.socket.extension.plugins.StreamMonitorPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/2/13
 */
public class HttpClientConfiguration {


    /**
     * smart-socket 插件
     */
    private final List<Plugin<Response>> plugins = new ArrayList<>();

    /**
     * 连接超时时间
     */
    private int connectTimeout;

    /**
     * 远程地址
     */
    private final String host;
    /**
     * 远程端口
     */
    private final int port;

    private ProxyConfig proxy;

    /**
     * read缓冲区大小
     */
    private int readBufferSize = 1024;


    /**
     * 缓冲池
     */
    private BufferPagePool writeBufferPool;

    /**
     * 缓冲池，必须是堆内缓冲区
     */
    private BufferPagePool readBufferPool;

    public HttpClientConfiguration(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 设置建立连接的超时时间
     */
    public HttpClientConfiguration connectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    /**
     * 设置 Http 代理服务器
     *
     * @param host     代理服务器地址
     * @param port     代理服务器端口
     * @param username 授权账户
     * @param password 授权密码
     */
    public HttpClientConfiguration proxy(String host, int port, String username, String password) {
        this.proxy = new ProxyConfig(host, port, username, password);
        return this;
    }

    /**
     * 连接代理服务器
     *
     * @param host 代理服务器地址
     * @param port 代理服务器端口
     */
    public HttpClientConfiguration proxy(String host, int port) {
        return this.proxy(host, port, null, null);
    }

    ProxyConfig getProxy() {
        return proxy;
    }

    public int readBufferSize() {
        return readBufferSize;
    }

    public HttpClientConfiguration readBufferSize(int readBufferSize) {
        this.readBufferSize = readBufferSize;
        return this;
    }

    public HttpClientConfiguration readBufferPool(BufferPagePool readBufferPool) {
        this.readBufferPool = readBufferPool;
        return this;
    }

    public HttpClientConfiguration writeBufferPool(BufferPagePool writeBufferPool) {
        this.writeBufferPool = writeBufferPool;
        return this;
    }

    BufferPagePool getWriteBufferPool() {
        return writeBufferPool;
    }

    BufferPagePool getReadBufferPool() {
        return readBufferPool;
    }

    /**
     * 启用 debug 模式后会打印码流
     */
    public HttpClientConfiguration debug(boolean debug) {
        plugins.removeIf(plugin -> plugin instanceof StreamMonitorPlugin);
        if (debug) {
            addPlugin(new StreamMonitorPlugin<>(StreamMonitorPlugin.BLUE_TEXT_INPUT_STREAM, StreamMonitorPlugin.RED_TEXT_OUTPUT_STREAM));
        }
        return this;
    }

    public HttpClientConfiguration addPlugin(Plugin<Response> plugin) {
        plugins.add(plugin);
        return this;
    }

    public List<Plugin<Response>> getPlugins() {
        return plugins;
    }
}
