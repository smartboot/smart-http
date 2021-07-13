/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: StringBodyCodec.java
 * Date: 2021-07-13
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode.body;

import org.smartboot.http.client.impl.Response;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.FixedLengthFrameDecoder;
import org.smartboot.http.common.utils.SmartDecoder;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public class StringBodyDecoder implements BodyDecoder {
    private SmartDecoder smartDecoder;

    @Override
    public boolean decode(ByteBuffer buffer, Response response) {
        if (smartDecoder == null) {
            int bodyLength = response.getContentLength();
            if (bodyLength > Constant.maxPostSize) {
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
