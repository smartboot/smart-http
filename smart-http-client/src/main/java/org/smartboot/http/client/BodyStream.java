/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: BodyStream.java
 * Date: 2021-07-17
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/7/17
 */
public interface BodyStream<T> {
    /**
     * 往缓冲区中写入数据
     */
    BodyStream<T> write(byte[] bytes, int offset, int len);

    /**
     * 往缓冲区中写入数据
     */
    default BodyStream<T> write(byte[] bytes) {
        write(bytes, 0, bytes.length);
        return this;
    }

    /**
     * 输出缓冲区的数据
     */
    BodyStream<T> flush();

    /**
     * 结束body流操作
     */
    T finish();
}
