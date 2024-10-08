/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Request.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.DecodeState;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.Reset;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.io.ReadListener;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.HttpUtils;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.Http2ServerHandler;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.ServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.socket.timer.HashedWheelTimer;
import org.smartboot.socket.timer.TimerTask;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.Attachment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public final class Request implements Reset {
    private static final Logger LOGGER = LoggerFactory.getLogger(Request.class);
    private static final Locale defaultLocale = Locale.getDefault();
    private static final int INIT_CONTENT_LENGTH = -2;
    private static final int NONE_CONTENT_LENGTH = -1;
    private final AioSession aioSession;


    /**
     * Http请求头
     */
    private final List<HeaderValue> headers = new ArrayList<>(8);
    private final HttpServerConfiguration configuration;
    private final DecoderUnit decodeState = new DecoderUnit();
    private ReadListener listener;
    /**
     * 请求参数
     */
    private Map<String, String[]> parameters;
    /**
     * 原始的完整请求
     */
    private String uri;
    private int headerSize = 0;
    /**
     * 请求方法
     */
    private String method;
    /**
     * Http协议版本
     */
    private String protocol = HttpProtocolEnum.HTTP_11.getProtocol();
    private String requestUri;
    private String requestUrl;
    private String contentType;
    private String connection;
    /**
     * 跟在URL后面的请求信息
     */
    private String queryString;
    /**
     * 协议
     */
    private String scheme;
    private long contentLength = INIT_CONTENT_LENGTH;
    private String remoteAddr;
    private String remoteHost;
    private String hostHeader;
    /**
     * 消息类型
     */
    private HttpTypeEnum type = null;
    /**
     * Post表单
     */
    private ByteBuffer formUrlencoded;
    private Cookie[] cookies;


    /**
     * 附件对象
     */
    private Attachment attachment;
    private HttpRequestImpl httpRequest;
    private Http2RequestImpl http2Request;
    private WebSocketRequestImpl webSocketRequest;
    private ServerHandler serverHandler;

    /**
     * 剩余可读字节数
     */
    private long remainingThreshold;

    /**
     * 最近一次IO时间
     */
    private long latestIo;
    private TimerTask httpIdleTask;
    private TimerTask wsIdleTask;

    void cancelHttpIdleTask() {
        synchronized (this) {
            if (httpIdleTask != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("cancel http idle monitor, request:{}", this);
                }
                httpIdleTask.cancel();
                httpIdleTask = null;
            }
        }
    }

    void cancelWsIdleTask() {
        synchronized (this) {
            if (wsIdleTask != null) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("cancel websocket idle monitor, request:{}", this);
                }
                wsIdleTask.cancel();
                wsIdleTask = null;
            }
        }
    }

    Request(HttpServerConfiguration configuration, AioSession aioSession) {
        this.configuration = configuration;
        this.aioSession = aioSession;
        this.remainingThreshold = configuration.getMaxRequestSize();
        if (configuration.getWsIdleTimeout() > 0) {
            wsIdleTask = HashedWheelTimer.DEFAULT_TIMER.scheduleWithFixedDelay(() -> {
                LOGGER.debug("check wsIdle monitor");
                if (System.currentTimeMillis() - latestIo > configuration.getWsIdleTimeout() && webSocketRequest != null) {
                    LOGGER.debug("close ws connection by idle monitor");
                    try {
                        aioSession.close();
                    } finally {
                        cancelWsIdleTask();
                        cancelHttpIdleTask();
                    }
                }
            }, configuration.getWsIdleTimeout(), TimeUnit.MILLISECONDS);
        }
        if (configuration.getHttpIdleTimeout() > 0) {
            httpIdleTask = HashedWheelTimer.DEFAULT_TIMER.scheduleWithFixedDelay(() -> {
                LOGGER.debug("check httpIdle monitor");
                if (System.currentTimeMillis() - latestIo > configuration.getHttpIdleTimeout() && webSocketRequest == null) {
                    LOGGER.debug("close http connection by idle monitor");
                    try {
                        aioSession.close();
                    } finally {
                        cancelHttpIdleTask();
                        cancelWsIdleTask();
                    }
                }
            }, configuration.getHttpIdleTimeout(), TimeUnit.MILLISECONDS);
        }
    }

    long getRemainingThreshold() {
        return remainingThreshold;
    }

    public AioSession getAioSession() {
        return aioSession;
    }

    public String getHost() {
        if (hostHeader == null) {
            hostHeader = getHeader(HeaderNameEnum.HOST.getName());
        }
        return hostHeader;
    }

    void decodeSize(int size) {
        remainingThreshold -= size;
        if (remainingThreshold < 0) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
    }


    public String getHeader(String headName) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(headName)) {
                return headerValue.getValue();
            }
        }
        return null;
    }


    public Collection<String> getHeaders(String name) {
        List<String> value = new ArrayList<>(4);
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(name)) {
                value.add(headerValue.getValue());
            }
        }
        return value;
    }


    public Collection<String> getHeaderNames() {
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < headerSize; i++) {
            nameSet.add(headers.get(i).getName());
        }
        return nameSet;
    }

    public int getHeaderSize() {
        return headerSize;
    }


    public BodyInputStream getInputStream() {
        throw new UnsupportedOperationException();
    }


    public void setHeader(String headerName, String value) {
        if (headerSize < headers.size()) {
            HeaderValue headerValue = headers.get(headerSize);
            headerValue.setName(headerName);
            headerValue.setValue(value);
        } else {
            headers.add(new HeaderValue(headerName, value));
        }
        headerSize++;
    }

    public HttpTypeEnum getRequestType() {
        if (type != null) {
            return type;
        }
        if (serverHandler instanceof WebSocketHandler) {
            type = HttpTypeEnum.WEBSOCKET;
        } else if (serverHandler instanceof Http2ServerHandler) {
            type = HttpTypeEnum.HTTP_2;
        } else {
            type = HttpTypeEnum.HTTP;
        }
        return type;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }


    public String getRequestURI() {
        return requestUri;
    }

    public void setRequestURI(String uri) {
        this.requestUri = uri;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMethod() {
        return method;
    }


    public boolean isSecure() {
        return configuration.isSecure();
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestURL() {
        if (requestUrl != null) {
            return requestUrl;
        }
        if (requestUri.startsWith("/")) {
            requestUrl = getScheme() + "://" + getHeader(HeaderNameEnum.HOST.getName()) + getRequestURI();
        } else {
            requestUrl = requestUri;
        }
        return requestUrl;
    }

    public String getScheme() {
        if (scheme == null) {
            return configuration.isSecure() ? Constant.SCHEMA_HTTPS : Constant.SCHEMA_HTTP;
        }
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HeaderNameEnum.CONTENT_TYPE.getName());
        return contentType;
    }

    public String getConnection() {
        if (connection != null) {
            return connection;
        }
        connection = getHeader(HeaderNameEnum.CONNECTION.getName());
        return connection;
    }


    public long getContentLength() {
        if (contentLength > INIT_CONTENT_LENGTH) {
            return contentLength;
        }
        //不包含content-length,则为：-1
        contentLength = NumberUtils.toLong(getHeader(HeaderNameEnum.CONTENT_LENGTH.getName()), NONE_CONTENT_LENGTH);
        if (contentLength >= remainingThreshold) {
            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
        }
        return contentLength;
    }


    public String getParameter(String name) {
        String[] arr = (name != null ? getParameterValues(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }


    public String[] getParameterValues(String name) {
        if (parameters != null) {
            return parameters.get(name);
        }
        parameters = new HashMap<>();
        //识别url中的参数
        String urlParamStr = queryString;
        if (StringUtils.isNotBlank(urlParamStr)) {
            urlParamStr = StringUtils.substringBefore(urlParamStr, "#");
            HttpUtils.decodeParamString(urlParamStr, parameters);
        }

        if (formUrlencoded != null) {
            HttpUtils.decodeParamString(new String(formUrlencoded.array()), parameters);
        }
        return getParameterValues(name);
    }


    public Map<String, String[]> getParameters() {
        if (parameters == null) {
            getParameter("");
        }
        return parameters;
    }

    /**
     * Returns the Internet Protocol (IP) address of the client
     * or last proxy that sent the request.
     * For HTTP servlets, same as the value of the
     * CGI variable <code>REMOTE_ADDR</code>.
     *
     * @return a <code>String</code> containing the
     * IP address of the client that sent the request
     */

    public String getRemoteAddr() {
        if (remoteAddr != null) {
            return remoteAddr;
        }
        try {
            InetSocketAddress remote = aioSession.getRemoteAddress();
            InetAddress address = remote.getAddress();
            if (address == null) {
                remoteAddr = remote.getHostString();
            } else {
                remoteAddr = address.getHostAddress();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return remoteAddr;
    }


    public InetSocketAddress getRemoteAddress() {
        try {
            return aioSession.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public InetSocketAddress getLocalAddress() {
        try {
            return aioSession.getLocalAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the fully qualified name of the client
     * or the last proxy that sent the request.
     * If the engine cannot or chooses not to resolve the hostname
     * (to improve performance), this method returns the dotted-string form of
     * the IP address. For HTTP servlets, same as the value of the CGI variable
     * <code>REMOTE_HOST</code>.
     *
     * @return a <code>String</code> containing the fully
     * qualified name of the client
     */

    public String getRemoteHost() {
        if (remoteHost != null) {
            return remoteHost;
        }
        try {
            remoteHost = aioSession.getRemoteAddress().getHostString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return remoteHost;
    }


    public Locale getLocale() {
        return getLocales().nextElement();
    }


    public Enumeration<Locale> getLocales() {
        Collection<String> acceptLanguage = getHeaders(HeaderNameEnum.ACCEPT_LANGUAGE.getName());
        if (acceptLanguage.isEmpty()) {
            return Collections.enumeration(Collections.singletonList(defaultLocale));
        }
        List<Locale> locales = new ArrayList<>();
        for (String language : acceptLanguage) {
            for (String lan : language.split(",")) {
                locales.add(Locale.forLanguageTag(lan));
            }
        }
        return Collections.enumeration(locales);
    }


    public String getCharacterEncoding() {
        return "utf8";
    }


    public Cookie[] getCookies() {
        if (cookies != null) {
            return cookies;
        }
        final List<Cookie> parsedCookies = new ArrayList<>();
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(HeaderNameEnum.COOKIE.getName())) {
                parsedCookies.addAll(HttpUtils.decodeCookies(headerValue.getValue()));
            }
        }
        cookies = new Cookie[parsedCookies.size()];
        parsedCookies.toArray(cookies);
        return cookies;
    }

    ByteBuffer getFormUrlencoded() {
        return formUrlencoded;
    }

    public void setFormUrlencoded(ByteBuffer formUrlencoded) {
        this.formUrlencoded = formUrlencoded;
    }

    /**
     * 获取附件对象
     *
     * @return 附件
     */
    public Attachment getAttachment() {
        return attachment;
    }

    /**
     * 存放附件，支持任意类型
     *
     * @param attachment 附件对象
     */
    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public HttpRequestImpl newHttpRequest() {
        if (httpRequest == null) {
            httpRequest = new HttpRequestImpl(this);
            cancelWsIdleTask();
        }
        return httpRequest;
    }

    public Http2RequestImpl newHttp2Request() {
        if (http2Request == null) {
            http2Request = new Http2RequestImpl(this);
            cancelWsIdleTask();
        }
        return http2Request;
    }

    public WebSocketRequestImpl newWebsocketRequest() {
        if (webSocketRequest == null) {
            webSocketRequest = new WebSocketRequestImpl(this);
            cancelHttpIdleTask();
        }
        return webSocketRequest;
    }

    public HttpServerConfiguration getConfiguration() {
        return configuration;
    }

    public void setLatestIo(long latestIo) {
        this.latestIo = latestIo;
    }


    public DecoderUnit getDecodeState() {
        return decodeState;
    }

    public void reset() {
        remainingThreshold = configuration.getMaxRequestSize();
        headerSize = 0;
        method = null;
        uri = null;
        requestUrl = null;
        parameters = null;
        contentType = null;
        contentLength = INIT_CONTENT_LENGTH;
        formUrlencoded = null;
        queryString = null;
        cookies = null;
        httpRequest = null;
        webSocketRequest = null;
        type = null;
        decodeState.setState(DecodeState.STATE_METHOD);
        scheme = null;
    }
}
