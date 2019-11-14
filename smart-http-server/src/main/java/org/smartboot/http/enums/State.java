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
    finished;
}