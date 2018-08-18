package org.smartboot.http.server.v1.decode;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/9
 */
class BufferRange {
    public int start = -1;
    public int length;
    public boolean isOk = false;

    public boolean isMatching = false;


    public void reset() {
        start = -1;
        length = 0;
        isOk = false;
        isMatching = false;
    }
}
