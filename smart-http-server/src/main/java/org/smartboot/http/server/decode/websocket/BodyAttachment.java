/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode.websocket;

import org.smartboot.http.common.utils.SmartDecoder;

/**
 * Http/Ws 请求解码附件对象
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
public class BodyAttachment {
    /**
     * 当前使用的解码器
     */
    private Decoder decoder;

    /**
     * Http Body解码器
     */
    private SmartDecoder bodyDecoder;

    public Decoder getDecoder() {
        return decoder;
    }

    public void setDecoder(Decoder decoder) {
        this.decoder = decoder;
    }

    public SmartDecoder getBodyDecoder() {
        return bodyDecoder;
    }

    public void setBodyDecoder(SmartDecoder bodyDecoder) {
        this.bodyDecoder = bodyDecoder;
    }

}
