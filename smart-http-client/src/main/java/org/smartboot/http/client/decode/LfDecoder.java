package org.smartboot.http.client.decode;

import org.smartboot.http.client.AbstractResponse;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;

public class LfDecoder implements HeaderDecoder {
    private final HeaderDecoder nextDecoder;

    public LfDecoder(HeaderDecoder nextDecoder) {
        this.nextDecoder = nextDecoder;
    }

    @Override
    public HeaderDecoder decode(ByteBuffer byteBuffer, AioSession aioSession, AbstractResponse response) {
        if (byteBuffer.hasRemaining()) {
            if (byteBuffer.get() != Constant.LF) {
                throw new HttpException(HttpStatus.BAD_REQUEST);
            }
            return nextDecoder.decode(byteBuffer, aioSession, response);
        }
        return this;
    }
}
