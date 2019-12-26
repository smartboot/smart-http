package org.smartboot.servlet.impl;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.logging.Logger;
import org.smartboot.http.logging.LoggerFactory;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletResponseImpl implements HttpServletResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletResponseImpl.class);
    private HttpResponse response;
    private HttpRequest request;
    private List<Cookie> cookies;
    private String contentType;

    public HttpServletResponseImpl(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return response.getHeader(name) != null;
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc) throws IOException {
        HttpStatus status = HttpStatus.valueOf(sc);
        sendError(sc, status != null ? status.getReasonPhrase() : "Unknow");
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        response.setHttpStatus(HttpStatus.FOUND);
        LOGGER.info("location:" + location);
        String redirect;
        if (ServletPathMatcher.isAbsoluteUrl(location)) {
            redirect = location;
        } else if (location.charAt(0) == '/') {
            redirect = request.getScheme() + "://" + getHeader(HttpHeaderConstant.Names.HOST) + location;
        } else {
            String url = request.getRequestURL();
            int last = url.lastIndexOf("/");
            if (last != 1) {
                redirect = url.substring(0, last + 1) + location;
            } else {
                redirect = url + location;
            }
        }
        LOGGER.info("sendRedirect:" + redirect);
        response.setHeader(HttpHeaderConstant.Names.LOCATION, redirect);
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        response.setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        response.addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        return response.getHttpStatus().value();
    }

    @Override
    public void setStatus(int sc) {
        response.setHttpStatus(HttpStatus.valueOf(sc));
    }

    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        response.setCharacterEncoding(charset);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        contentType = type;
        response.setContentType(type);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentLength(int len) {
        setContentLengthLong(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        response.setContentLength((int) len);
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException();
    }
}
