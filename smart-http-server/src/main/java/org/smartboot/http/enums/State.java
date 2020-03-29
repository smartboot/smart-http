/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: State.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.enums;

/**
 * Http解码状态机
 */
public enum State {
    method,
    uri,
    queryString,
    protocol,
    request_line_end,
    head_name,
    head_value,
    head_line_LF,
    head_line_end,
    head_finished,
    body,
    finished,
    ws_handshake,
    ws_data;
}