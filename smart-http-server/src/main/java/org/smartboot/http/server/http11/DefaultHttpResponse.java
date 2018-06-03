/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: DefaultHttpResponse.java
 * Date: 2018-02-17
 * Author: sandao
 */

package org.smartboot.http.server.http11;

import org.smartboot.http.common.HttpEntity;
import org.smartboot.http.common.HttpHeader;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/2/3
 */
public class DefaultHttpResponse extends HttpEntity implements HttpResponse {

    /**
     * http响应码
     */
    private HttpStatus httpStatus;

    private HttpOutputStream outputStream;

    private DefaultHttpResponse(HttpHeader header) {
        super(header);
    }

    public DefaultHttpResponse(AioSession<HttpEntity> session, Http11Request request, HttpHandle responseHandle) {
        this(new HttpHeader());
        this.outputStream = new HttpOutputStream(session, this, request, responseHandle);
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public void write(ByteBuffer buffer) throws IOException {
        outputStream.write(buffer);
    }
}
