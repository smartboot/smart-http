/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: DefaultHttpLifecycle.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.ResponseHandler;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.GzipUtils;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/15
 */
public class DefaultHttpResponseHandler extends ResponseHandler {
    private ResponseHandler responseHandler;

    @Override
    public boolean onBodyStream(ByteBuffer buffer, Response baseHttpResponse) {
        if (responseHandler != null) {
            return responseHandler.onBodyStream(buffer, baseHttpResponse);
        }
        String transferEncoding = baseHttpResponse.getHeader(HeaderNameEnum.TRANSFER_ENCODING.getName());
        if (StringUtils.equals(transferEncoding, HeaderValueEnum.CHUNKED.getName())) {
            if (responseHandler == null) {
                responseHandler = new ChunkedHttpLifecycle();
            }
        } else if (baseHttpResponse.getContentLength() > 0) {
            if (responseHandler == null) {
                responseHandler = new ContentLengthHttpLifecycle();
            }
        } else {
            return true;
        }
        return responseHandler.onBodyStream(buffer, baseHttpResponse);
    }

    /**
     * @author 三刀（zhengjunweimail@163.com）
     * @version V1.0 , 2021/7/12
     */
    public static class ChunkedHttpLifecycle extends ResponseHandler {
        private final ByteArrayOutputStream body = new ByteArrayOutputStream();
        private PART part = PART.CHUNK_LENGTH;
        private SmartDecoder chunkedDecoder;

        @Override
        public boolean onBodyStream(ByteBuffer buffer, Response response) {
            switch (part) {
                case CHUNK_LENGTH:
                    return decodeChunkedLength(buffer, response);
                case CHUNK_CONTENT:
                    return decodeChunkedContent(buffer, response);
                case CHUNK_END:
                    return decodeChunkedEnd(buffer, response);
                default:
                    throw new IllegalStateException();
            }
        }

        private boolean decodeChunkedContent(ByteBuffer buffer, Response response) {
            if (chunkedDecoder.decode(buffer)) {
                try {
                    body.write(chunkedDecoder.getBuffer().array());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                part = PART.CHUNK_END;
                return onBodyStream(buffer, response);
            }
            return false;
        }

        private boolean decodeChunkedEnd(ByteBuffer buffer, Response response) {
            if (buffer.remaining() < 2) {
                return false;
            }
            if (buffer.get() == Constant.CR && buffer.get() == Constant.LF) {
                part = PART.CHUNK_LENGTH;
                return onBodyStream(buffer, response);
            }
            throw new IllegalStateException();
        }

        private boolean decodeChunkedLength(ByteBuffer buffer, Response response) {
            int length = StringUtils.scanUntilAndTrim(buffer, Constant.LF);
            if (length < 0) {
                return false;
            }
            if (length == 1) {
                finishDecode(response);
                return true;
            }
            String contentLength = StringUtils.convertToString(buffer, buffer.position() - length - 1, length - 1);
            int chunkedLength = Integer.parseInt(contentLength, 16);
            if (chunkedLength == 0) {
                return onBodyStream(buffer, response);
            }
            part = PART.CHUNK_CONTENT;
            chunkedDecoder = new FixedLengthFrameDecoder(chunkedLength);
            return onBodyStream(buffer, response);
        }

        public void finishDecode(Response response) {
            if (StringUtils.equals(HeaderValueEnum.GZIP.getName(), response.getHeader(HeaderNameEnum.CONTENT_ENCODING.getName()))) {
                response.setBody(GzipUtils.uncompressToString(body.toByteArray()));
            } else {
                response.setBody(body.toString());
            }
        }

        enum PART {
            CHUNK_LENGTH,
            CHUNK_CONTENT,
            CHUNK_END
        }
    }

    /**
     * @author 三刀（zhengjunweimail@163.com）
     * @version V1.0 , 2021/7/12
     */
    public static class ContentLengthHttpLifecycle extends ResponseHandler {
        private SmartDecoder smartDecoder;

        @Override
        public boolean onBodyStream(ByteBuffer buffer, Response response) {
            if (smartDecoder == null) {
                int bodyLength = response.getContentLength();
                if (bodyLength > Constant.maxBodySize) {
                    throw new HttpException(HttpStatus.PAYLOAD_TOO_LARGE);
                }
                smartDecoder = new FixedLengthFrameDecoder(response.getContentLength());
            }
            if (smartDecoder.decode(buffer)) {
                response.setBody(new String(smartDecoder.getBuffer().array(), Charset.forName(response.getCharacterEncoding())));
                return true;
            }
            return false;
        }
    }
}
