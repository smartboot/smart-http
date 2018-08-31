/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: DefaultHandle.java
 * Date: 2018-02-08
 * Author: sandao
 */

package org.smartboot.http.server.handle.http11;

import org.apache.commons.lang.StringUtils;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.util.QuickTimerTask;

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
    private SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private String date = "";

    public ResponseHandle() {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        new ResponseDateTimer();
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
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
        response.setHeader(HttpHeaderConstant.Names.DATE, date);

        doNext(request, response);
    }

    public class ResponseDateTimer extends QuickTimerTask {

        @Override
        protected long getPeriod() {
            return 5000;
        }

        @Override
        public void run() {
            ResponseHandle.this.date = sdf.format(new Date());
        }
    }
}
