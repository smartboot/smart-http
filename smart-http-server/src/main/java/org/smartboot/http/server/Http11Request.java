/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Http11Request.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.enums.State;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.utils.Consts;
import org.smartboot.http.utils.DelimiterFrameDecoder;
import org.smartboot.http.utils.EmptyInputStream;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.http.utils.PostInputStream;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
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
public final class Http11Request implements HttpRequest {
    private static final Locale defaultLocale = Locale.getDefault();
    /**
     * 解码状态
     */
    State _state = State.method;
    /**
     * 原始的完整请求
     */
    String _originalUri;
    String tmpHeaderName;
    /**
     * Header Value解码器是否启用
     */
    boolean headValueDecoderEnable = false;
    private int headerSize = 0;
    /**
     * Http请求头
     */
    private List<HeaderValue> headers = new ArrayList<>(8);
    /**
     * 请求方法
     */
    private HttpMethodEnum method;
    /**
     * Http协议版本
     */
    private String protocol;
    /**
     * 消息头Value值解码器
     */
    private DelimiterFrameDecoder headerValueDecoder = null;
    /**
     * 请求参数
     */
    private Map<String, String[]> parameters;
    private InputStream inputStream;
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
    private String scheme = Consts.SCHEMA_HTTP;
    private int contentLength = -1;
    private AioSession<Http11Request> aioSession;

    private String remoteAddr;

    private String remoteHost;
    /**
     * Http响应
     */
    private Http11Response response;

    private String hostHeader;


    private byte[] postData = null;

    Http11Request(AioSession<Http11Request> aioSession) {
        this.aioSession = aioSession;
        response = new Http11Response(this, aioSession.writeBuffer());
    }

    Http11Response getResponse() {
        return response;
    }

    public String getHost() {
        if (hostHeader == null) {
            hostHeader = getHeader(HttpHeaderConstant.Names.HOST);
        }
        return hostHeader;
    }

    @Override
    public String getHeader(String headName) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(headName)) {
                return headerValue.getValue();
            }
        }
        return null;
    }

    @Override
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

    @Override
    public Collection<String> getHeaderNames() {
        Set<String> nameSet = new HashSet<>();
        for (int i = 0; i < headerSize; i++) {
            nameSet.add(headers.get(i).getName());
        }
        return nameSet;
    }


    void setHeader(String headerName, String value) {
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
    public InputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        if (method != HttpMethodEnum.POST) {
            inputStream = new EmptyInputStream();
        } else if (postData == null) {
            inputStream = new PostInputStream(aioSession.getInputStream(getContentLength()), getContentLength());
        } else {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return inputStream;
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    void setRequestURI(String uri) {
        this.requestUri = uri;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getMethod() {
        return method.getMethod();
    }

    void setMethod(HttpMethodEnum method) {
        this.method = method;
    }

    HttpMethodEnum getMethodEnum() {
        return method;
    }

    @Override
    public String getRequestURL() {
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


    public String getScheme() {
        return scheme;
    }

    void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getQueryString() {
        return queryString;
    }

    void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HttpHeaderConstant.Names.CONTENT_TYPE);
        return contentType;
    }

    @Override
    public int getContentLength() {
        if (contentLength > -1) {
            return contentLength;
        }
        contentLength = NumberUtils.toInt(getHeader(HttpHeaderConstant.Names.CONTENT_LENGTH), -1);
        return contentLength;
    }

    @Override
    public String getParameter(String name) {
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
            decodeParamString(urlParamStr, parameters);
        }
        //识别body中的参数
        parsePostParameters();
        return getParameterValues(name);
    }

    void setPostData(byte[] postData) {
        this.postData = postData;
    }

    private void parsePostParameters() {
        if (postData != null && postData.length > 0) {
            decodeParamString(new String(postData), parameters);
        }
    }


    @Override
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
    @Override
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

    @Override
    public InetSocketAddress getRemoteAddress() {
        try {
            return aioSession.getRemoteAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
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
    @Override
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

    @Override
    public Locale getLocale() {
        return defaultLocale;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(Arrays.asList(defaultLocale));
    }


    @Override
    public String getCharacterEncoding() {
        return "utf8";
    }

    private void decodeParamString(String paramStr, Map<String, String[]> paramMap) {
        if (StringUtils.isBlank(paramStr)) {
            return;
        }
        String[] uriParamStrArray = StringUtils.split(paramStr, "&");
        for (String param : uriParamStrArray) {
            int index = param.indexOf("=");
            if (index == -1) {
                continue;
            }
            try {
                String key = StringUtils.substring(param, 0, index);
                String value = URLDecoder.decode(StringUtils.substring(param, index + 1), "utf8");
                String[] values = paramMap.get(key);
                if (values == null) {
                    paramMap.put(key, new String[]{value});
                } else {
                    String[] newValue = new String[values.length + 1];
                    System.arraycopy(values, 0, newValue, 0, values.length);
                    newValue[values.length] = value;
                    paramMap.put(key, newValue);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取解码器
     *
     * @return
     */
    public DelimiterFrameDecoder getHeaderValueDecoder() {
        if (headerValueDecoder == null) {
            headerValueDecoder = new DelimiterFrameDecoder(new byte[]{Consts.CR}, 1024);
        }
        return headerValueDecoder;
    }

    void rest() {
        _state = State.method;
        headerSize = 0;
        method = null;
        tmpHeaderName = null;
        headValueDecoderEnable = false;
        if (headerValueDecoder != null) {
            headerValueDecoder.reset();
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        _originalUri = null;
        requestUrl = null;
        parameters = null;
        contentType = null;
        contentLength = -1;
        response.reset();
    }

//    @Override
//    public String toString() {
//        return ToStringBuilder.reflectionToString(this);
//    }
}
