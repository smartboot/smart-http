/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: HttpV2Entity.java
 * Date: 2018-01-23
 * Author: sandao
 */

package org.smartboot.http.server.v1.decode;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.enums.MethodEnum;
import org.smartboot.http.enums.State;
import org.smartboot.http.utils.EmptyInputStream;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.extension.decoder.FixedLengthFrameDecoder;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http消息体，兼容请求与响应
 *
 * @author 三刀 2018/06/02
 */
public class HttpEntity implements HttpRequest {
    protected final BufferRange methodRange = new BufferRange();
    protected final BufferRange uriRange = new BufferRange();
    protected final BufferRange protocolRange = new BufferRange();
    protected final BufferRanges headerRanges = new BufferRanges();
    protected int initPosition = 0;
    State state = State.method;
    ByteBuffer buffer;
    private FixedLengthFrameDecoder bodyForm;
    private int currentPosition = 0;

    private MethodEnum methodEnum;

    private String originalUri;
    private String requestUri;
    private String protocol;
    private String contentType;
    private int contentLength;
    private String queryString;
    private InputStream inputStream = null;

    private Map<String, String> headMap = new HashMap<>();


    public void rest() {
        methodRange.reset();
        uriRange.reset();
        protocolRange.reset();
        headerRanges.reset();
        buffer = null;
        initPosition = 0;
        setCurrentPosition(0);
        state = State.method;
        bodyForm = null;
        originalUri = null;
        requestUri = null;
        protocol = null;
        contentType = null;
        contentLength = 0;
        inputStream = null;
        headMap.clear();
    }

    public void decodeHead() {
        getMethodRange();
        originalUri = get(uriRange);
        protocol = get(protocolRange);
        for (BufferRange bufferRange : headerRanges.headers) {
            if (!bufferRange.isOk || bufferRange.isMatching) {
                continue;
            }
            String headStr = get(bufferRange);
            headMap.put(StringUtils.substringBefore(headStr, ":"), StringUtils.substringAfter(headStr, ":").trim());
        }
    }


    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public byte[] getBytes(BufferRange range) {
        return getBytes(range, 0);
    }

    byte[] getBytes(BufferRange range, int offset) {
        int p = buffer.position();
        byte[] b = new byte[range.length - offset];
        buffer.position(range.start + offset);
        buffer.get(b);
        buffer.position(p);
        return b;
    }

    public String get(BufferRange range) {
        return new String(getBytes(range));
    }

    public MethodEnum getMethodRange() {
        return methodEnum == null ? methodEnum = MethodEnum.getByMethod(buffer, methodRange.start, methodRange.length)
                : methodEnum;
    }

    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = headMap.get(HttpHeaderConstant.Names.CONTENT_TYPE);
        if (contentType != null) {
            return contentType;
        }
        BufferRange bufferRange = getAndRemove(HttpHeaderConstant.HeaderBytes.CONTENT_TYPE);
        if (bufferRange != null) {
            contentType = new String(getBytes(bufferRange, HttpHeaderConstant.HeaderBytes.CONTENT_TYPE.length + 1)).trim();
            headMap.put(HttpHeaderConstant.Names.CONTENT_TYPE, contentType);
        }
        return contentType;
    }

    public int getContentLength() {
        if (contentLength > 0) {
            return contentLength;
        }
        if (headMap.containsKey(HttpHeaderConstant.Names.CONTENT_LENGTH)) {
            contentLength = NumberUtils.toInt(headMap.get(HttpHeaderConstant.Names.CONTENT_LENGTH));
            return contentLength;
        }
        BufferRange bufferRange = getAndRemove(HttpHeaderConstant.HeaderBytes.CONTENT_LENGTH);
        if (bufferRange != null) {
            contentLength = NumberUtils.toInt(new String(getBytes(bufferRange, HttpHeaderConstant.HeaderBytes.CONTENT_LENGTH.length + 1)).trim());
            headMap.put(HttpHeaderConstant.Names.CONTENT_LENGTH, contentType);
        }
        return contentLength;
    }

    BufferRange getAndRemove(byte[] contentType) {
        final List<BufferRange> headers = headerRanges.headers;
        for (BufferRange bufferRange : headers) {
            if (!bufferRange.isOk || bufferRange.length < contentType.length || bufferRange.isMatching) {
                continue;
            }
            int pos = bufferRange.start;
            boolean ok = true;
            for (byte b : contentType) {
                if (b != buffer.get(pos++)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                bufferRange.isMatching = true;
                return bufferRange;
            }

        }
        return null;
    }

    public String getProtocol() {
        if (protocol != null) {
            return protocol;
        }
        if (!protocolRange.isMatching) {
            protocol = get(protocolRange);
            protocolRange.isMatching = true;
        }
        return protocol;
    }

    FixedLengthFrameDecoder getBodyForm() {
        return bodyForm;
    }

    void setBodyForm(FixedLengthFrameDecoder bodyForm) {
        this.bodyForm = bodyForm;
    }

    @Override
    public String getHeader(String headName) {
        if (headMap.containsKey(headName)) {
            return headMap.get(headName);
        }
        byte[] bytes = headName.getBytes();
        BufferRange bufferRange = getAndRemove(bytes);
        String val = null;
        if (bufferRange != null) {
            val = new String(getBytes(bufferRange, bytes.length + 1)).trim();
        }
        headMap.put(headName, val);
        return val;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream == null ? new EmptyInputStream() : inputStream;
    }

    public String getOriginalUri() {
        if (originalUri != null) {
            return originalUri;
        }
        originalUri = get(uriRange);
        uriRange.isMatching = true;
        return originalUri;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getRequestURI() {
        return requestUri;
    }

    public void setRequestURI(String requestUri) {
        this.requestUri = requestUri;
    }
}
