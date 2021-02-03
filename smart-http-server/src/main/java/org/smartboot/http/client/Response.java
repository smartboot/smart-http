/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpResponse.java
 * Date: 2021-02-02
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.enums.YesNoEnum;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.HttpUtils;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class Response implements HttpResponse {
    private static final Locale defaultLocale = Locale.getDefault();
    private static final int INIT_CONTENT_LENGTH = -2;
    private static final int NONE_CONTENT_LENGTH = -1;
    private final AioSession aioSession;
    /**
     * Http请求头
     */
    private final List<HeaderValue> headers = new ArrayList<>(8);

    private String headerTemp;
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
     * Http协议版本
     */
    private String protocol;
    private String requestUri;
    private String requestUrl;
    private String contentType;
    /**
     * 跟在URL后面的请求信息
     */
    private String queryString;
    /**
     * 协议
     */
    private String scheme = Constant.SCHEMA_HTTP;
    private int contentLength = INIT_CONTENT_LENGTH;
    private String remoteAddr;

    private String remoteHost;

    private String hostHeader;


    private YesNoEnum websocket = null;

    /**
     * Post表单
     */
    private String formUrlencoded;

    private Cookie[] cookies;

    /**
     * 附件对象
     */
    private Object attachment;

    private int statusCode;

    private String statusDesc;

    Response(AioSession aioSession) {
        this.aioSession = aioSession;
    }

    public AioSession getAioSession() {
        return aioSession;
    }

    public final String getHost() {
        if (hostHeader == null) {
            hostHeader = getHeader(HttpHeaderConstant.Names.HOST);
        }
        return hostHeader;
    }

    @Override
    public final String getHeader(String headName) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(headName)) {
                return headerValue.getValue();
            }
        }
        return null;
    }

    @Override
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

    @Override
    public final Collection<String> getHeaderNames() {
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < headerSize; i++) {
            nameSet.add(headers.get(i).getName());
        }
        return nameSet;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    public final void setHeadValue(String value) {
        setHeader(headerTemp, value);
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


    @Override
    public final String getRequestURI() {
        return requestUri;
    }

    public final void setRequestURI(String uri) {
        this.requestUri = uri;
    }

    @Override
    public final String getProtocol() {
        return protocol;
    }

    public final void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public String getUri() {
        return uri;
    }

    public final void setUri(String uri) {
        this.uri = uri;
    }

    public void setHeaderTemp(String headerTemp) {
        this.headerTemp = headerTemp;
    }

    @Override
    public final String getRequestURL() {
        if (requestUrl != null) {
            return requestUrl;
        }
        if (requestUri.startsWith("/")) {
            requestUrl = getScheme() + "://" + getHeader(HttpHeaderConstant.Names.HOST) + getRequestURI();
        } else {
            requestUrl = requestUri;
        }
        return requestUrl;
    }

    public final String getScheme() {
        return scheme;
    }

    final void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public final String getQueryString() {
        return queryString;
    }

    public final void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public final String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HttpHeaderConstant.Names.CONTENT_TYPE);
        return contentType;
    }

    @Override
    public final int getContentLength() {
        if (contentLength > INIT_CONTENT_LENGTH) {
            return contentLength;
        }
        //不包含content-length,则为：-1
        contentLength = NumberUtils.toInt(getHeader(HttpHeaderConstant.Names.CONTENT_LENGTH), NONE_CONTENT_LENGTH);
        return contentLength;
    }

    @Override
    public final String getParameter(String name) {
        String[] arr = (name != null ? getParameterValues(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
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
            HttpUtils.decodeParamString(formUrlencoded, parameters);
        }
        return getParameterValues(name);
    }


    @Override
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
    @Override
    public final String getRemoteAddr() {
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

    @Override
    public final InetSocketAddress getRemoteAddress() {
        try {
            return aioSession.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public final InetSocketAddress getLocalAddress() {
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
    @Override
    public final String getRemoteHost() {
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

    @Override
    public final Locale getLocale() {
        return defaultLocale;
    }

    @Override
    public final Enumeration<Locale> getLocales() {
        return Collections.enumeration(Collections.singletonList(defaultLocale));
    }

    @Override
    public final String getCharacterEncoding() {
        return "utf8";
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies != null) {
            return cookies;
        }
        String cookieValue = getHeader(HttpHeaderConstant.Names.COOKIE);
        if (StringUtils.isBlank(cookieValue)) {
            return new Cookie[0];
        }
        List<Cookie> cookieList = new ArrayList<>();

        int state = 1;// 1:name ,2:value
        int startIndex = 0;
        int endIndex = 0;
        String name = null;
        String value;
        for (int i = 0; i < cookieValue.length(); i++) {
            switch (state) {
                case 1:
                    //查找name
                    if (cookieValue.charAt(i) == '=') {
                        endIndex = i;
                        while (cookieValue.charAt(startIndex) == ' ' && startIndex < i) {
                            startIndex++;
                        }
                        while (cookieValue.charAt(endIndex - 1) == ' ' && endIndex > startIndex) {
                            endIndex--;
                        }
                        name = cookieValue.substring(startIndex, endIndex);
                        startIndex = i + 1;
                        state = 2;
                    }
                    break;
                case 2:
                    //查找value
                    if (cookieValue.charAt(i) == ';') {
                        endIndex = i;
                        while (cookieValue.charAt(startIndex) == ' ' && startIndex < i) {
                            startIndex++;
                        }
                        while (cookieValue.charAt(endIndex - 1) == ' ' && endIndex > startIndex) {
                            endIndex--;
                        }
                        value = cookieValue.substring(startIndex, endIndex);
                        cookieList.add(new Cookie(name, value));
                        startIndex = i + 1;
                        state = 1;
                    }
                    break;
            }
        }
        //最后一个value为空字符串情况
        if (startIndex == cookieValue.length()) {
            cookieList.add(new Cookie(name, ""));
        } else if (endIndex < startIndex) {
            endIndex = cookieValue.length();
            while (cookieValue.charAt(startIndex) == ' ') {
                startIndex++;
            }
            while (cookieValue.charAt(endIndex - 1) == ' ' && endIndex > startIndex) {
                endIndex--;
            }
            value = cookieValue.substring(startIndex, endIndex);
            cookieList.add(new Cookie(name, value));
        }
        cookies = new Cookie[cookieList.size()];
        cookieList.toArray(cookies);
        return cookies;
    }

    public final YesNoEnum isWebsocket() {
        return websocket;
    }

    public final void setWebsocket(YesNoEnum websocket) {
        this.websocket = websocket;
    }

    public String getFormUrlencoded() {
        return formUrlencoded;
    }

    public void setFormUrlencoded(String formUrlencoded) {
        this.formUrlencoded = formUrlencoded;
    }


    /**
     * 获取附件对象
     *
     * @param <A> 附件对象类型
     * @return 附件
     */
    public final <A> A getAttachment() {
        return (A) attachment;
    }

    /**
     * 存放附件，支持任意类型
     *
     * @param <A>        附件对象类型
     * @param attachment 附件对象
     */
    public final <A> void setAttachment(A attachment) {
        this.attachment = attachment;
    }

    public void reset() {
        headerSize = 0;
        uri = null;
        requestUrl = null;
        parameters = null;
        contentType = null;
        contentLength = INIT_CONTENT_LENGTH;
        formUrlencoded = null;
        queryString = null;
        cookies = null;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
}
