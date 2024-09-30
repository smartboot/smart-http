/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.http.common.DecodeState;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
class DecoderUnit extends DecodeState {

    private String decodeHeaderName;

    private AbstractResponse response;


    public AbstractResponse getResponse() {
        return response;
    }

    public void setResponse(AbstractResponse response) {
        this.response = response;
    }


    public String getDecodeHeaderName() {
        return decodeHeaderName;
    }

    public void setDecodeHeaderName(String decodeHeaderName) {
        this.decodeHeaderName = decodeHeaderName;
    }
}
