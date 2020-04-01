/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestLineDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.BaseHttpRequest;
import org.smartboot.http.utils.Constant;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
public class HttpHeaderDecoder implements Decoder {

    private final HttpHeaderEndDecoder decoder = new HttpHeaderEndDecoder();

    @Override
    public Decoder deocde(ByteBuffer byteBuffer, char[] cacheChars, AioSession<BaseHttpRequest> aioSession, BaseHttpRequest request) {
        int length = StringUtils.scanUntilAndTrim(byteBuffer, Constant.LF, cacheChars, true);
        if (length != -1) {
            if (cacheChars[length - 1] != Constant.CR) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
            //head end
            if (length == 1) {
                return decoder.deocde(byteBuffer, cacheChars, aioSession, request);
            }
            int colonIndex = 0;
            for (; colonIndex < length; colonIndex++) {
                if (cacheChars[colonIndex] == Constant.COLON) {
                    break;
                }
            }
            if (colonIndex == length) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
            int offset = 0;
            int end = colonIndex;
            while (cacheChars[offset] == Constant.SP) {
                offset++;
            }
            while (cacheChars[end] == Constant.SP) {
                end--;
            }
            String name = StringUtils.convertToString(cacheChars, offset, end - offset, StringUtils.String_CACHE_HEADER_VALUE);

            offset = colonIndex + 1;
            end = length - 1;

            while (cacheChars[offset] == Constant.SP) {
                offset++;
            }
            while (cacheChars[end] == Constant.SP) {
                end--;
            }
            String value = StringUtils.convertToString(cacheChars, offset, end - offset, StringUtils.String_CACHE_HEADER_VALUE);
            request.setHeader(name, value);
            return this.deocde(byteBuffer, cacheChars, aioSession, request);
        } else {
            return this;
        }
    }
}
