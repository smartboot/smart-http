/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpHeaderDecoder.java
 * Date: 2021-07-15
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpHeaderDecoder implements HeaderDecoder {

    private final HeaderValueDecoder headerValueDecoder = new HeaderValueDecoder();
    private final LfDecoder lfDecoder = new LfDecoder(this);

    @Override
    public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse request) {
        if (byteBuffer.remaining() < 2) {
            return this;
        }

        //header解码结束
        byteBuffer.mark();
        if (byteBuffer.get() == Constant.CR) {
            if (byteBuffer.get() != Constant.LF) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
            return HeaderDecoder.BODY_READY_DECODER;
        }
        byteBuffer.reset();
        //Header name解码
        ByteTree<?> name = StringUtils.scanByteTree(byteBuffer, ByteTree.COLON_END_MATCHER, ByteTree.DEFAULT);
        if (name == null) {
            return this;
        }
//        System.out.println("headerName: " + name);
        request.setHeaderTemp(name.getStringValue());
        return headerValueDecoder.decode(byteBuffer, aioSession, request);
    }

    /**
     * Value值解码
     */
    class HeaderValueDecoder implements HeaderDecoder {
        @Override
        public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse request) {
            ByteTree<?> value = StringUtils.scanByteTree(byteBuffer, ByteTree.CR_END_MATCHER, ByteTree.DEFAULT);
            if (value == null) {
                if (byteBuffer.remaining() == byteBuffer.capacity()) {
                    throw new HttpException(HttpStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
                }
                return this;
            }
//            System.out.println("value: " + value);
            request.setHeadValue(value.getStringValue());
            return lfDecoder.decode(byteBuffer, aioSession, request);
        }
    }
}
