/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BufferOutputStream.java
 * Date: 2020-12-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http;

import org.smartboot.socket.buffer.VirtualBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/7
 */
public abstract class BufferOutputStream extends OutputStream {
    public abstract void write(ByteBuffer buffer) throws IOException;

    public abstract void write(VirtualBuffer virtualBuffer) throws IOException;
}
