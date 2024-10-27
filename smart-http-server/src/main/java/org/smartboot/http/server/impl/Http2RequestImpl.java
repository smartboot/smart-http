package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.Reset;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.io.ReadListener;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.Part;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.socket.util.Attachment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Http2RequestImpl implements HttpRequest, Reset {
    private static final int INIT_CONTENT_LENGTH = -2;
    private static final int NONE_CONTENT_LENGTH = -1;
    public static final int STATE_HEADER_FRAME = 0;
    public static final int STATE_DATA_FRAME = 1;
    public static final int STATE_DONE = 2;
    private int state = STATE_HEADER_FRAME;
    private final Map<String, HeaderValue> headers = new HashMap<>();
    private final int streamId;
    private ByteArrayOutputStream body;
    private BodyInputStream bodyInputStream = BodyInputStream.EMPTY_INPUT_STREAM;
    private final Http2ResponseImpl response;
    /**
     * 请求方法
     */
    private String method;
    private String requestUri;
    private String requestUrl;
    private String contentType;
    private long contentLength = 1;

    public Http2RequestImpl(int streamId, Request request) {
        this.streamId = streamId;
        response = new Http2ResponseImpl(streamId, request);
    }

    public Map<String, HeaderValue> getHeaders() {
        return headers;
    }

    public void checkState(int state) {
        if (this.state != state) {
            throw new IllegalStateException("state:" + state + " not equals " + this.state);
        }
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public void reset() {

    }

    @Override
    public String getHeader(String headName) {
        HeaderValue headerValue = headers.get(headName);
        return headerValue == null ? null : headerValue.getValue();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        HeaderValue headerValue = headers.get(name);
        if (headerValue == null) {
            return Collections.emptyList();
        }
        List<String> values = new ArrayList<>(4);
        do {
            values.add(headerValue.getValue());
            headerValue = headerValue.getNextValue();
        } while (headerValue != null);
        return values;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public BodyInputStream getInputStream() throws IOException {
        return bodyInputStream;
    }

    public String getRequestURI() {
        return requestUri;
    }

    public void setRequestURI(String uri) {
        this.requestUri = uri;
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
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

    @Override
    public String getQueryString() {
        return "";
    }

    public String getContentType() {
        if (contentType != null) {
            return contentType;
        }
        contentType = getHeader(HeaderNameEnum.CONTENT_TYPE.getName());
        return contentType;
    }


    public long getContentLength() {
        if (contentLength > INIT_CONTENT_LENGTH) {
            return contentLength;
        }
        //不包含content-length,则为：-1
        contentLength = NumberUtils.toLong(getHeader(HeaderNameEnum.CONTENT_LENGTH.getName()), NONE_CONTENT_LENGTH);
//        if (contentLength >= remainingThreshold) {
//            throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
//        }
        return contentLength;
    }

    @Override
    public String getParameter(String name) {
        return "";
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts(MultipartConfig configElement) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        return "";
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return "";
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    public int getStreamId() {
        return streamId;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public Attachment getAttachment() {
        return null;
    }

    @Override
    public void setAttachment(Attachment attachment) {

    }

    public ByteArrayOutputStream getBody() {
        return body;
    }

    public void setBody(ByteArrayOutputStream body) {
        this.body = body;
    }

    public void bodyDone() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(body.toByteArray());
        bodyInputStream = new BodyInputStream(null) {
            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return inputStream.read(b, off, len);
            }
        };
    }

    public AbstractResponse getResponse() {
        return response;
    }
}
