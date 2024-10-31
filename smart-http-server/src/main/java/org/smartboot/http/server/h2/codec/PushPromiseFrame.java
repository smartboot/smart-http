package org.smartboot.http.server.h2.codec;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.server.h2.hpack.DecodingCallback;
import org.smartboot.http.server.impl.Http2Session;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PushPromiseFrame extends Http2Frame {


    private int padLength;
    private int promisedStream;
    private ByteBuffer fragment;
    private byte[] padding = EMPTY_PADDING;
    private Collection<HeaderValue> headers;

    public PushPromiseFrame(Http2Session http2Session, int streamId, int flags, int remaining) {
        super(http2Session, streamId, flags, remaining);
    }


    @Override
    public boolean decode(ByteBuffer buffer) {
        if (finishDecode()) {
            return true;
        }
        switch (state) {
            case STATE_PAD_LENGTH:
                if (getFlag(FLAG_PADDED)) {
                    if (!buffer.hasRemaining()) {
                        return false;
                    }
                    padLength = buffer.get();
                    if (padLength < 0) {
                        throw new IllegalStateException();
                    }
                    remaining = -1;
                }
                state = STATE_STREAM_ID;
            case STATE_STREAM_ID:
                if (buffer.remaining() < 4) {
                    return false;
                }
                promisedStream = buffer.getInt();
                remaining = 4;
                state = STATE_FRAGMENT;
                fragment = ByteBuffer.allocate(remaining - padLength);
            case STATE_FRAGMENT:
                int min = Math.min(buffer.remaining(), fragment.remaining());
                int limit = buffer.limit();
                buffer.limit(buffer.position() + min);
                fragment.put(buffer);
                buffer.limit(limit);
                remaining -= min;
                if (fragment.hasRemaining()) {
                    return false;
                }
                fragment.flip();
                try {
                    Map<String, HeaderValue> headers = new HashMap<>();
                    session.getHpackDecoder().decode(fragment, getFlag(FLAG_END_HEADERS), new DecodingCallback() {
                        @Override
                        public void onDecoded(CharSequence name, CharSequence value) {
                            System.out.println(name + ":" + value);
                            if (headers.containsKey(name)) {
                                headers.get(name).setNextValue(new HeaderValue(name.toString(), value.toString()));
                            } else {
                                headers.put(name.toString(), new HeaderValue(name.toString(), value.toString()));
                            }
                        }
                    });
                    this.headers = headers.values();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                state = STATE_PADDING;
            case STATE_PADDING:
                if (buffer.remaining() < padLength) {
                    return false;
                }
                if (padLength > 0) {
                    padding = new byte[padLength];
                    buffer.get(padding);
                    remaining -= padLength;
                }
        }
        checkEndRemaining();
        return true;
    }

    @Override
    public void writeTo(WriteBuffer writeBuffer) throws IOException {
        int payloadLength = 0;
        byte flags = (byte) this.flags;

        // Calculate payload length and set flags
        boolean padded = padding != null && padding.length > 0;
        if (padded) {
            payloadLength += 1 + padding.length;
            flags |= FLAG_PADDED;
        }

        for (HeaderValue header : headers) {
            session.getHpackEncoder().header(header.getName(), header.getValue());
        }
        fragment = ByteBuffer.allocate(1024);
        if (session.getHpackEncoder().encode(fragment)) {
            System.out.println("success");
        }
        fragment.flip();
        payloadLength += fragment.remaining();

        // Write frame header
        writeBuffer.writeInt(payloadLength << 8 | FRAME_TYPE_PUSH_PROMISE);
        writeBuffer.writeByte(flags);
        System.out.println("write push promise header ,streamId:" + streamId);
        writeBuffer.writeInt(streamId);

        // Write pad length if padded
        if (padded) {
            writeBuffer.writeByte((byte) padding.length);
        }

        // Write stream dependency and weight if priority is set
        writeBuffer.writeInt(promisedStream);

        // Write fragment

        writeBuffer.write(fragment.array(), 0, fragment.remaining());

        // Write padding if padded
        if (padded) {
            writeBuffer.write(padding);
        }
    }

    @Override
    public int type() {
        return FRAME_TYPE_PUSH_PROMISE;
    }


    public int getPadLength() {
        return padLength;
    }

    public int getPromisedStream() {
        return promisedStream;
    }

    public void setHeaders(Collection<HeaderValue> headers) {
        this.headers = headers;
    }

    public Collection<HeaderValue> getHeaders() {
        return headers;
    }
}
