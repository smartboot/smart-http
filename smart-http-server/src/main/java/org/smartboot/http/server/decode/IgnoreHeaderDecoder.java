/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: IgnoreHeaderDecoder.java
 * Date: 2021-04-10
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/10
 */
public class IgnoreHeaderDecoder implements Decoder {

    @Override
    public Decoder decode(ByteBuffer byteBuffer, AioSession aioSession, Request httpHeader) {
        int position = byteBuffer.position();
        int limit = byteBuffer.limit();
        byte[] data = byteBuffer.array();

        while (limit - position >= 4) {
            byte b = data[position + 3 + byteBuffer.arrayOffset()];
            // 第四位非CR或LF，则前4位必然不会是header结束符，可跳跃4位
            if (b > Constant.CR || (b != Constant.CR && b != Constant.LF)) {
                position += 4;
                continue;
            }
            int index = 0;
            while (data[byteBuffer.arrayOffset() + position++] == Constant.HEADER_END[index]) {
                index++;
            }
            if (index == Constant.HEADER_END.length) {
                byteBuffer.position(position);
                return HttpRequestProtocol.BODY_READY_DECODER;
            }
        }
        byteBuffer.position(position);
        return this;
    }
}
