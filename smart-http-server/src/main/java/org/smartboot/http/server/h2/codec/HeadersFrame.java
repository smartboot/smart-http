package org.smartboot.http.server.h2.codec;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.server.h2.hpack.Encoder;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

public class HeadersFrame extends Http2Frame {

    public static final int TYPE = 0x1;

    private int padLength;
    private int streamDependency;
    private int weight;
    private boolean exclusive;
    private ByteBuffer fragment;
    private byte[] padding = EMPTY_PADDING;
    private Collection<HeaderValue> headers;

    public HeadersFrame(int streamId, int flags, int remaining) {
        super(streamId, flags, remaining);
    }

    public HeadersFrame(int streamId) {
        super(streamId, FLAG_END_HEADERS, 0);
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
                state = STATE_STREAM_DEPENDENCY;
            case STATE_STREAM_DEPENDENCY:
                if (getFlag(FLAG_PRIORITY)) {
                    if (buffer.remaining() < 5) {
                        return false;
                    }
                    streamDependency = buffer.getInt();
                    weight = buffer.get() & 0xFF;
                    remaining = 5;
                }
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
                HpackDecoder hpackDecoder = new HpackDecoder(128);
                try {
                    headers = hpackDecoder.decode(fragment);
                } catch (HpackDecoder.HpackException e) {
                    throw new RuntimeException("Failed to decode HPACK headers", e);
                }
                // Now 'headers' contains the decoded HTTP/2 headers
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
        if (weight > 0) {
            payloadLength += 5;
            flags |= FLAG_PRIORITY;
        }
        // Encode headers
//        HpackEncoder encoder = new HpackEncoder(4096);
        Encoder encoder = new Encoder(4096);
        for (HeaderValue header : headers) {
            encoder.header(header.getName(), header.getValue());
        }
        fragment = ByteBuffer.allocate(1024);
        if (encoder.encode(fragment)) {
            System.out.println("success");
        }
        fragment.flip();
        payloadLength += fragment.remaining();

        // Write frame header
        writeBuffer.writeInt(payloadLength << 8 | TYPE);
        writeBuffer.writeByte(flags);
        writeBuffer.writeInt(streamId);

        // Write pad length if padded
        if (padded) {
            writeBuffer.writeByte((byte) padding.length);
        }

        // Write stream dependency and weight if priority is set
        if (hasFlag(flags, FLAG_PRIORITY)) {
            writeBuffer.writeInt(streamDependency);
            writeBuffer.writeByte((byte) weight);
        }

        // Write fragment

        writeBuffer.write(fragment.array(), 0, fragment.remaining());

        // Write padding if padded
        if (padded) {
            writeBuffer.write(padding);
        }
    }

    public void setHeaders(Collection<HeaderValue> headers) {
        this.headers = headers;
    }

    @Override
    public int type() {
        return TYPE;
    }


}
