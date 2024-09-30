package org.smartboot.http.common;

public class DecodeState {
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
