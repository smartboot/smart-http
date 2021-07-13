/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: ChunkedBodyCodec.java
 * Date: 2021-07-13
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode.body;

import org.smartboot.http.client.impl.Response;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.GzipUtils;
import org.smartboot.http.common.utils.SmartDecoder;
import org.smartboot.http.common.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public class ChunkedBodyDecoder implements BodyDecoder {
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private PART part = PART.CHUNK_LENGTH;
    private SmartDecoder chunkedDecoder;

    @Override
    public boolean decode(ByteBuffer buffer, Response response) {
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
            return decode(buffer, response);
        }
        return false;
    }

    private boolean decodeChunkedEnd(ByteBuffer buffer, Response response) {
        if (buffer.remaining() < 2) {
            return false;
        }
        if (buffer.get() == Constant.CR && buffer.get() == Constant.LF) {
            part = PART.CHUNK_LENGTH;
            return decode(buffer, response);
        }
        throw new IllegalStateException();
    }

    private boolean decodeChunkedLength(ByteBuffer buffer, Response response) {
        int length = StringUtils.scanUntilAndTrim(buffer, Constant.LF);
        if (length < 0) {
            return false;
        }
        if (length == 1) {
            finishDecode(response, body);
            return true;
        }
        String contentLength = StringUtils.convertToString(buffer, buffer.position() - length - 1, length - 1);
        int chunkedLength = Integer.parseInt(contentLength, 16);
        if (chunkedLength == 0) {
            return decode(buffer, response);
        }
        part = PART.CHUNK_CONTENT;
        chunkedDecoder = new FixedLengthFrameDecoder(chunkedLength);
        return decode(buffer, response);
    }

    public void finishDecode(Response response, OutputStream outputStream) {
        if (StringUtils.equals("gzip", response.getHeader(HeaderNameEnum.CONTENT_ENCODING.getName()))) {
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
