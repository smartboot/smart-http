/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: WebSocketResponse.java
 * Date: 2021-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client.impl;

import org.smartboot.http.common.enums.WebsocketStatus;

/**
 * WebSocket消息请求接口
 *
 * @author 三刀
 * @version V1.0 , 2020/4/1
 */
public interface WebSocketResponse {
    public WebsocketStatus getWebsocketStatus();

    public int getFrameOpcode();

    public byte[] getPayload();

}
