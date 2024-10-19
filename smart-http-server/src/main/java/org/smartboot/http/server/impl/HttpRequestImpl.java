/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpRequestImpl.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.io.BodyInputStream;
import org.smartboot.http.common.io.ChunkedInputStream;
import org.smartboot.http.common.io.PostInputStream;
import org.smartboot.http.common.io.ReadListener;
import org.smartboot.http.common.multipart.MultipartConfig;
import org.smartboot.http.common.multipart.Part;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.server.decode.MultipartFormDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class HttpRequestImpl extends AbstractRequest {
    /**
     * 释放维持长连接
     */
    private boolean keepAlive;
    private List<Part> parts;
    private boolean multipartParsed;
    /**
     * Http Body解码器
     */
    private SmartDecoder bodyDecoder;
    /**
     * 空流
     */
    private static final BodyInputStream EMPTY_INPUT_STREAM = new BodyInputStream(null) {
        @Override
        public void setReadListener(ReadListener listener) {
            throw new IllegalStateException();
        }

        @Override
        public int read(byte[] b, int off, int len) {
            return -1;
        }

        @Override
        public int available() {
            return 0;
        }

        @Override
        public void close() {

        }
    };
    private final HttpResponseImpl response;
    private BodyInputStream inputStream;
    private Map<String, String> trailerFields;

    HttpRequestImpl(Request request) {
        init(request);
        this.response = new HttpResponseImpl(this);
    }

    public final AbstractResponse getResponse() {
        return response;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public BodyInputStream getInputStream() throws IOException {
        if (inputStream != null) {
            return inputStream;
        }
        if (request.getFormUrlencoded() != null || multipartParsed) {
            inputStream = EMPTY_INPUT_STREAM;
        }
        //如果一个消息即存在传输译码（Transfer-Encoding）头域并且也 Content-Length 头域，后者会被忽略。
        else if (HeaderValueEnum.CHUNKED.getName().equalsIgnoreCase(request.getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName()))) {
            inputStream = new ChunkedInputStream(request.getAioSession(), request.getRemainingThreshold(), stringStringMap -> HttpRequestImpl.this.trailerFields = stringStringMap);
        } else {
            long contentLength = getContentLength();
            if (contentLength > 0) {
                inputStream = new PostInputStream(request.getAioSession(), contentLength);
            } else {
                inputStream = EMPTY_INPUT_STREAM;
            }
        }
        return inputStream;
    }

    @Override
    public Map<String, String> getTrailerFields() {
        return trailerFields == null ? super.getTrailerFields() : trailerFields;
    }

    @Override
    public boolean isTrailerFieldsReady() {
        return !HeaderValueEnum.CHUNKED.getName().equals(getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName())) || trailerFields != null;
    }

    public SmartDecoder getBodyDecoder() {
        return bodyDecoder;
    }

    public void setBodyDecoder(SmartDecoder bodyDecoder) {
        this.bodyDecoder = bodyDecoder;
    }

    public void reset() {
        request.reset();
        response.reset();
        trailerFields = null;
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if (parts != null) {
            for (Part part : parts) {
                try {
                    part.delete();
                } catch (IOException ignore) {
                }
            }
            parts = null;
        }
        multipartParsed = false;
    }

    public Collection<Part> getParts(MultipartConfig configElement) throws IOException {
        if (!multipartParsed) {
            MultipartFormDecoder multipartFormDecoder = new MultipartFormDecoder(this, configElement);
            long remaining = getContentLength();
            if (configElement.getMaxRequestSize() > 0 && configElement.getMaxRequestSize() < remaining) {
                throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
            }
            int p = request.getAioSession().readBuffer().position();
            while (!multipartFormDecoder.decode(request.getAioSession().readBuffer(), this)) {
                remaining -= request.getAioSession().readBuffer().position() - p;
                int readSize = request.getAioSession().read();
                p = request.getAioSession().readBuffer().position();
                if (readSize == -1) {
                    break;
                }
            }
            multipartParsed = true;
            remaining -= request.getAioSession().readBuffer().position() - p;
            if (remaining != 0) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
        }
        if (parts == null) {
            parts = new ArrayList<>();
        }
        return parts;
    }

    public void setPart(Part part) {
        if (parts == null) {
            parts = new ArrayList<>();
        }
        this.parts.add(part);
    }

}
