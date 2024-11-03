/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Request.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpTypeEnum;
import org.smartboot.http.common.io.BodyInputStream;
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
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.Attachment;

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

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public abstract class CommonRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRequest.class);
    private static final Locale defaultLocale = Locale.getDefault();
    private static final int INIT_CONTENT_LENGTH = -2;
    private static final int NONE_CONTENT_LENGTH = -1;

    protected final AioSession aioSession;

    /**
     * Http请求头
     */
    protected final List<HeaderValue> headers = new ArrayList<>(8);
    protected final HttpServerConfiguration configuration;
    /**
     * 请求参数
     */
    protected Map<String, String[]> parameters;
    /**
     * 原始的完整请求
     */
    protected String uri;
    protected int headerSize = 0;
    /**
     * 请求方法
     */
    protected String method;
    /**
     * Http协议版本
     */
    protected String protocol = HttpProtocolEnum.HTTP_11.getProtocol();
    protected String requestUri;
    protected String requestUrl;
    protected String contentType;
    protected String connection;
    /**
     * 跟在URL后面的请求信息
     */
    protected String queryString;
    /**
     * 协议
     */
    protected String scheme;
    protected long contentLength = INIT_CONTENT_LENGTH;
    protected String remoteAddr;
    protected String remoteHost;
    protected String hostHeader;
    /**
     * 消息类型
     */
    protected HttpTypeEnum type = null;
    /**
     * Post表单
     */
    protected ByteBuffer formUrlencoded;
    protected Cookie[] cookies;


    /**
     * 附件对象
     */
    protected Attachment attachment;

    protected ServerHandler serverHandler;


    CommonRequest(AioSession aioSession, HttpServerConfiguration configuration) {
        this.aioSession = aioSession;
        this.configuration = configuration;
    }


    public final String getHost() {
        if (hostHeader == null) {
            hostHeader = getHeader(HeaderNameEnum.HOST.getName());
        }
        return hostHeader;
    }


    public final String getHeader(String headName) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(headName)) {
                return headerValue.getValue();
            }
        }
        return null;
    }


    public final Collection<String> getHeaders(String name) {
        List<String> value = new ArrayList<>(4);
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(name)) {
                value.add(headerValue.getValue());
            }
        }
        return value;
    }


    public final Collection<String> getHeaderNames() {
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < headerSize; i++) {
            nameSet.add(headers.get(i).getName());
        }
        return nameSet;
    }

    public final int getHeaderSize() {
        return headerSize;
    }


    public BodyInputStream getInputStream() {
        throw new UnsupportedOperationException();
    }


    public final void setHeader(String headerName, String value) {
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

    public void setType(HttpTypeEnum type) {
        this.type = type;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }


    public final String getRequestURI() {
        return requestUri;
    }

    public final void setRequestURI(String uri) {
        this.requestUri = uri;
    }


    public final String getProtocol() {
        return protocol;
    }

    public final void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public final String getMethod() {
        return method;
    }


    public final boolean isSecure() {
        return configuration.isSecure();
    }

    public final void setMethod(String method) {
        this.method = method;
    }

    public final String getUri() {
        return uri;
    }

    public final void setUri(String uri) {
        this.uri = uri;
    }

    public final String getRequestURL() {
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

    public final String getScheme() {
        if (scheme == null) {
            return configuration.isSecure() ? Constant.SCHEMA_HTTPS : Constant.SCHEMA_HTTP;
        }
        return scheme;
    }

    public final void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public final String getQueryString() {
        return queryString;
    }

    public final void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    public final String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HeaderNameEnum.CONTENT_TYPE.getName());
        return contentType;
    }

    public final String getConnection() {
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

        return contentLength;
    }


    public final String getParameter(String name) {
        String[] arr = (name != null ? getParameterValues(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }


    public final String[] getParameterValues(String name) {
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


    public final Map<String, String[]> getParameters() {
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

    public abstract String getRemoteAddr();


    public abstract InetSocketAddress getRemoteAddress();


    public abstract InetSocketAddress getLocalAddress();

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

    public abstract String getRemoteHost();


    public final Locale getLocale() {
        return getLocales().nextElement();
    }


    public final Enumeration<Locale> getLocales() {
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


    public final String getCharacterEncoding() {
        return "utf8";
    }


    public final Cookie[] getCookies() {
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

    final ByteBuffer getFormUrlencoded() {
        return formUrlencoded;
    }

    final public void setFormUrlencoded(ByteBuffer formUrlencoded) {
        this.formUrlencoded = formUrlencoded;
    }

    /**
     * 获取附件对象
     *
     * @return 附件
     */
    public final Attachment getAttachment() {
        return attachment;
    }

    /**
     * 存放附件，支持任意类型
     *
     * @param attachment 附件对象
     */
    public final void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public AioSession getAioSession() {
        return aioSession;
    }

    public final HttpServerConfiguration getConfiguration() {
        return configuration;
    }

}
