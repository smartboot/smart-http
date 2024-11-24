/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.TimerUtils;
import org.smartboot.http.server.HttpServerConfiguration;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Semaphore;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
final class HttpOutputStream extends AbstractOutputStream {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static byte[] SERVER_LINE = null;
    private static final Date currentDate = new Date(0);
    private static final Semaphore flushDateSemaphore = new Semaphore(1);
    private static long expireTime;
    private static byte[] dateBytes;
    private final Request request;
    private final HttpServerConfiguration configuration;
    private static final byte[] CHUNKED = "Transfer-Encoding: chunked\r\n".getBytes();
    private static final byte[] DATE_END_1 = "Date:E, dd MMM yyyy HH:mm:ss z\r\n".getBytes();
    private static final byte[] DATE_END_2 = "Date:E, dd MMM yyyy HH:mm:ss z\r\n\r\n".getBytes();
    private static long expireTime_1;
    private static long expireTime_2;

    static {
        flushDate();
    }

    public HttpOutputStream(HttpRequestImpl httpRequest, HttpResponseImpl response) {
        super(httpRequest.request, response);
        this.request = httpRequest.request;
        this.configuration = request.getConfiguration();
        if (SERVER_LINE == null) {
            SERVER_LINE = (HeaderNameEnum.SERVER.getName() + Constant.COLON_CHAR + configuration.serverName() + Constant.CRLF).getBytes();
        }
    }

    private static long flushDate() {
        long currentTime = TimerUtils.currentTimeMillis();
        if (currentTime > expireTime && flushDateSemaphore.tryAcquire()) {
            try {
                expireTime = currentTime + 1000;
                currentDate.setTime(currentTime);
                String date = sdf.format(currentDate);
                dateBytes = date.getBytes();
            } finally {
                flushDateSemaphore.release();
            }
        }
        return currentTime;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        if (configuration.getWsIdleTimeout() > 0 || configuration.getHttpIdleTimeout() > 0) {
            request.setLatestIo(System.currentTimeMillis());
        }
    }

    protected void writeHeadPart(boolean hasHeader) throws IOException {
        checkChunked();

        long contentLength = response.getContentLength();
        String contentType = response.getContentType();
        if (contentLength > 0) {
            remaining = contentLength;
        }

        // HTTP/1.1
        writeBuffer.write(request.getProtocol().getProtocolBytesWithSP());

        // Status
        HttpStatus httpStatus = response.getHttpStatus();
        httpStatus.write(writeBuffer);

        if (contentType != null) {
            writeBuffer.write(HeaderNameEnum.CONTENT_TYPE.getBytesWithColon());
            writeString(contentType);
            writeBuffer.write(Constant.CRLF_BYTES);
        }
        if (contentLength >= 0) {
            writeBuffer.write(HeaderNameEnum.CONTENT_LENGTH.getBytesWithColon());
            writeLongString(contentLength);
            writeBuffer.write(Constant.CRLF_BYTES);
        } else if (chunkedSupport) {
            writeBuffer.write(CHUNKED);
        }

        if (configuration.serverName() != null && response.getHeader(HeaderNameEnum.SERVER.getName()) == null) {
            writeBuffer.write(SERVER_LINE);
        }

        // Date
        long currentTime = flushDate();
        if (hasHeader) {
            if (currentTime > expireTime_1) {
                expireTime_1 = currentTime + 1000;
                System.arraycopy(dateBytes, 0, DATE_END_1, 5, 25);
            }
            writeBuffer.write(DATE_END_1);
        } else {
            if (currentTime > expireTime_2) {
                expireTime_2 = currentTime + 1000;
                System.arraycopy(dateBytes, 0, DATE_END_2, 5, 25);
            }
            writeBuffer.write(DATE_END_2);
        }
    }

    private void checkChunked() {
        if (!chunkedSupport) {
            return;
        }
        if (response.getContentLength() >= 0) {
            disableChunked();
        } else if (response.getHttpStatus().value() == HttpStatus.CONTINUE.value() || response.getHttpStatus().value() == HttpStatus.SWITCHING_PROTOCOLS.value()) {
            disableChunked();
        } else if (HttpMethodEnum.HEAD.name().equals(request.getMethod())) {
            disableChunked();
        } else if (HttpProtocolEnum.HTTP_11 != request.getProtocol()) {
            disableChunked();
        } else if (response.getContentType().startsWith(HeaderValueEnum.CONTENT_TYPE_EVENT_STREAM.getName())) {
            disableChunked();
        }
    }
}
