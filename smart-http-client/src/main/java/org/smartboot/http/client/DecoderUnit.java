/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: RequestAttachment.java
 * Date: 2021-05-26
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/26
 */
class DecoderUnit {
    public static final int STATE_PROTOCOL_DECODE = 0;
    public static final int STATE_STATUS_CODE = 1;
    public static final int STATE_STATUS_DESC = 1 << 1;
    public static final int STATE_STATUS_END = 1 << 2;
    public static final int STATE_HEADER_END_CHECK = 1 << 3;
    public static final int STATE_HEADER_NAME = 1 << 4;
    public static final int STATE_HEADER_VALUE = 1 << 5;
    public static final int STATE_HEADER_LINE_END = 1 << 6;

    public static final int STATE_HEADER_CALLBACK = 1 << 7;
    public static final int STATE_BODY = 1 << 8;
    public static final int STATE_FINISH = 1 << 9;
    /**
     * HTTP响应报文解析状态
     */
    private int state;
    private String decodeHeaderName;

    private AbstractResponse response;


    public AbstractResponse getResponse() {
        return response;
    }

    public void setResponse(AbstractResponse response) {
        this.response = response;
    }


    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getDecodeHeaderName() {
        return decodeHeaderName;
    }

    public void setDecodeHeaderName(String decodeHeaderName) {
        this.decodeHeaderName = decodeHeaderName;
    }
}
