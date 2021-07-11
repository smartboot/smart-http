/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestImpl.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.utils.PostInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
final class HttpRequestImpl extends AbstractRequest {
    /**
     * 空流
     */
    private static final InputStream EMPTY_INPUT_STREAM = new InputStream() {
        @Override
        public int read() {
            return -1;
        }
    };
    private final HttpResponseImpl response;
    private InputStream inputStream;

    HttpRequestImpl(Request request) {
        init(request);
        this.response = new HttpResponseImpl(this, request.getAioSession());
    }

    public final HttpResponseImpl getResponse() {
        return response;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (request.getMethod() == HttpMethodEnum.CONNECT.getMethod()) {
            RequestAttachment requestAttachment = request.getAioSession().getAttachment();
            ByteBuffer buffer = requestAttachment.getProxyContent();
            return buffer == null ? EMPTY_INPUT_STREAM : new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int min = Math.min(len, buffer.remaining());
                    buffer.get(b, off, min);
                    return min;
                }

                @Override
                public int available() throws IOException {
                    return buffer.remaining();
                }
            };
        }
        if (inputStream != null) {
            return inputStream;
        }
        int contentLength = getContentLength();
        if (contentLength > 0 && request.getFormUrlencoded() == null) {
            inputStream = new PostInputStream(request.getAioSession().getInputStream(contentLength), contentLength);
        } else {
            inputStream = EMPTY_INPUT_STREAM;
        }
        return inputStream;
    }


    public void reset() {
        getRequest().reset();
        response.reset();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
    }

}
