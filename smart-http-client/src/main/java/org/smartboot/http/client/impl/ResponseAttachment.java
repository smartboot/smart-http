/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.client.decode.HeaderDecoder;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
public class ResponseAttachment {

    private AbstractResponse response;

    private HeaderDecoder decoder;


    public AbstractResponse getResponse() {
        return response;
    }

    public void setResponse(AbstractResponse response) {
        this.response = response;
    }

    public HeaderDecoder getDecoder() {
        return decoder;
    }

    public void setDecoder(HeaderDecoder decoder) {
        this.decoder = decoder;
    }
}
