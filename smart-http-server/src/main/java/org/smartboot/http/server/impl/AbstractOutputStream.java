/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: AbstractOutputStream.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.CommonOutputStream;
import org.smartboot.http.common.Cookie;
import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
abstract class AbstractOutputStream extends CommonOutputStream {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private static final Semaphore flushDateSemaphore = new Semaphore(1);
    private static final Date currentDate = new Date(0);
    protected static byte[] date;
    private static String SERVER_ALIAS_NAME = "smart-http";

    static {
        String aliasServer = System.getProperty("smartHttp.server.alias");
        if (aliasServer != null) {
            SERVER_ALIAS_NAME = aliasServer + "smart-http";
        }
        flushDate();
    }

    protected final AbstractResponse response;
    protected final HttpRequest request;

    public AbstractOutputStream(HttpRequest request, AbstractResponse response, WriteBuffer writeBuffer) {
        super(writeBuffer);
        this.response = response;
        this.request = request;
    }

    private static void flushDate() {
        if ((System.currentTimeMillis() - currentDate.getTime() > 990) && flushDateSemaphore.tryAcquire()) {
            try {
                currentDate.setTime(System.currentTimeMillis());
                AbstractOutputStream.date = ("\r\n" + HeaderNameEnum.DATE.getName() + ":" + sdf.format(currentDate) + "\r\n"
                        + HeaderNameEnum.SERVER.getName() + ":" + SERVER_ALIAS_NAME + "\r\n\r\n").getBytes();
            } finally {
                flushDateSemaphore.release();
            }
        }
    }

    /**
     * 输出Http消息头
     */
    protected final void writeHead() throws IOException {
        if (committed) {
            return;
        }

        //输出http状态行、contentType,contentLength、Transfer-Encoding、server等信息
        writeBuffer.write(getHeadPart());

        //转换Cookie
        convertCookieToHeader();

        //输出Header部分
        writeHeader();

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        flushDate();
        writeBuffer.write(date);
        committed = true;
    }

    private void convertCookieToHeader() {
        List<Cookie> cookies = response.getCookies();
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        cookies.forEach(cookie -> response.addHeader(HeaderNameEnum.SET_COOKIE.getName(), cookie.toString()));
    }

    protected abstract byte[] getHeadPart();

    private void writeHeader() throws IOException {
        if (response.getHeaders() != null) {
            for (Map.Entry<String, HeaderValue> entry : response.getHeaders().entrySet()) {
                HeaderValue headerValue = entry.getValue();
                while (headerValue != null) {
                    writeBuffer.write(getHeaderNameBytes(entry.getKey()));
                    writeBuffer.write(getBytes(headerValue.getValue()));
                    headerValue = headerValue.getNextValue();
                }
            }
        }
    }

    @Override
    protected final void check() {
        if (HttpMethodEnum.HEAD.getMethod().equals(request.getMethod())) {
            throw new UnsupportedOperationException(request.getMethod() + " can not write http body");
        }
    }
}
