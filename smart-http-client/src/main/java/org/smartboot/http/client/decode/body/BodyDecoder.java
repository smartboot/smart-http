/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BodyCodec.java
 * Date: 2021-07-13
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.decode.body;

import org.smartboot.http.client.impl.Response;

import java.nio.ByteBuffer;

/**
 * Http body解码器
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/12
 */
public interface BodyDecoder {

    boolean decode(ByteBuffer buffer, Response response);
}
