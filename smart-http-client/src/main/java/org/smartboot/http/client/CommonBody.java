package org.smartboot.http.client;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2023/2/13
 */
class CommonBody<T extends IHttpRest> implements Body<T> {
    private final Body<? extends IHttpRest> body;
    private final T rest;


    public CommonBody(Body<? extends IHttpRest> body, T rest) {
        this.body = body;
        this.rest = rest;
    }

    @Override
    public Body<T> write(byte[] bytes, int offset, int len) {
        body.write(bytes, offset, len);
        return this;
    }

    @Override
    public final Body<T> flush() {
        body.flush();
        return this;
    }

    @Override
    public final T done() {
        return rest;
    }
}
