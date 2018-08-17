/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: DefaultHandle.java
 * Date: 2018-02-08
 * Author: sandao
 */

package org.smartboot.http.server.handle.http11;

import org.apache.commons.lang.StringUtils;
import org.smartboot.http.common.HttpEntity;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.http11.HttpResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/8
 */
public class ResponseHandle extends HttpHandle {
    private ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
        }
    };

    public static void main(String[] args) {
        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        System.out.println(sdf.format(new Date()));
    }

    @Override
    public void doHandle(HttpEntity request, HttpResponse response) throws IOException {
        if (response.getHttpStatus() == null) {
            response.setHttpStatus(HttpStatus.OK);
        }
        if (response.getHeader(HttpHeaderConstant.Names.CONTENT_LENGTH) == null
                && response.getHeader(HttpHeaderConstant.Names.TRANSFER_ENCODING) == null
                && response.getHttpStatus() == HttpStatus.OK) {
            response.setHeader(HttpHeaderConstant.Names.TRANSFER_ENCODING, HttpHeaderConstant.Values.CHUNKED);
        }
        if (response.getHeader(HttpHeaderConstant.Names.SERVER) == null) {
            response.setHeader(HttpHeaderConstant.Names.SERVER, "smart-socket");
        }
        if (response.getHeader(HttpHeaderConstant.Names.HOST) == null) {
            response.setHeader(HttpHeaderConstant.Names.HOST, "localhost");
        }

        if (StringUtils.equalsIgnoreCase(request.getHeader(HttpHeaderConstant.Names.CONNECTION), HttpHeaderConstant.Values.KEEPALIVE)) {
            response.setHeader(HttpHeaderConstant.Names.CONNECTION, HttpHeaderConstant.Values.KEEPALIVE);
        }

        /**
         * RFC2616 3.3.1
         * 只能用 RFC 1123 里定义的日期格式来填充头域 (header field)的值里用到 HTTP-date 的地方
         */
        response.setHeader(HttpHeaderConstant.Names.DATE, simpleDateFormatThreadLocal.get().format(new Date()));

        doNext(request, response);
    }
}
