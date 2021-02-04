/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Response.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.HttpResponse;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.enums.YesNoEnum;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.http.utils.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/2
 */
public class Response implements HttpResponse {
    private static final int INIT_CONTENT_LENGTH = -2;
    private static final int NONE_CONTENT_LENGTH = -1;
    /**
     * Http请求头
     */
    private final List<HeaderValue> headers = new ArrayList<>(8);

    private String headerTemp;

    private int headerSize = 0;

    /**
     * Http协议版本
     */
    private String protocol;
    private String contentType;
    private int contentLength = INIT_CONTENT_LENGTH;

    private YesNoEnum websocket = null;

    /**
     * body内容
     */
    private String body;

    /**
     * 附件对象
     */
    private Object attachment;

    private int statusCode;

    private String statusDesc;

    private String encoding;

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
    public final String getProtocol() {
        return protocol;
    }

    public final void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHeaderTemp(String headerTemp) {
        this.headerTemp = headerTemp;
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
    public final String getCharacterEncoding() {
        if (encoding != null) {
            return encoding;
        }
        String contentType = getContentType();
        String charset = StringUtils.substringAfter(contentType, "charset=");
        if (StringUtils.isNotBlank(charset)) {
            this.encoding = Charset.forName(charset).name();
        } else {
            this.encoding = "utf8";
        }
        return this.encoding;
    }

    public final YesNoEnum isWebsocket() {
        return websocket;
    }

    public final void setWebsocket(YesNoEnum websocket) {
        this.websocket = websocket;
    }

    public String body() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
