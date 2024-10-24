package org.smartboot.http.server.impl;

import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.Reset;
import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.Part;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.socket.util.Attachment;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Http2RequestImpl implements HttpRequest, Reset {
    public static final int STATE_HEADER_FRAME = 0;
    public static final int STATE_DATA_FRAME = 1;
    public static final int STATE_DONE = 2;
    private int state = STATE_HEADER_FRAME;
    private final Map<String, HeaderValue> headers = new HashMap<>();
    private ByteBuffer readBuffer;
    private final int streamId;

    public Http2RequestImpl(int streamId) {
        this.streamId = streamId;
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
        return null;
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public String getMethod() {
        return "";
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
        return "";
    }

    @Override
    public String getQueryString() {
        return "";
    }

    @Override
    public String getContentType() {
        return "";
    }

    @Override
    public long getContentLength() {
        return 0;
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

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setReadBuffer(ByteBuffer readBuffer) {
        this.readBuffer = readBuffer;
    }
}
