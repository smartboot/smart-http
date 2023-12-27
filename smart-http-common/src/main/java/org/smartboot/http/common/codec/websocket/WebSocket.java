package org.smartboot.http.common.codec.websocket;

import org.smartboot.socket.util.Attachment;

import java.nio.ByteBuffer;

public interface WebSocket {
    public static final Decoder PAYLOAD_FINISH = new Decoder() {
        @Override
        public Decoder decode(ByteBuffer byteBuffer, WebSocket request) {
            return this;
        }
    };

    public boolean isFrameFinalFlag();

    public void setFrameFinalFlag(boolean frameFinalFlag);

    public boolean isFrameMasked();

    public void setFrameMasked(boolean frameMasked);

    public int getFrameRsv();

    public void setFrameRsv(int frameRsv);

    public int getFrameOpcode();

    public void setFrameOpcode(int frameOpcode);

    public byte[] getPayload();

    public long getPayloadLength();

    public void setPayloadLength(long payloadLength);

    public byte[] getMaskingKey();

    public void setMaskingKey(byte[] maskingKey);

    Attachment getAttachment();

    public void setPayload(byte[] payload);
}
