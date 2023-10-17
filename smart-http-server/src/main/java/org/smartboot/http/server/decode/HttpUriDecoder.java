/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: UriDecoder.java
 * Date: 2020-03-30
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server.decode;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.common.utils.ByteTree;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.ServerHandler;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

/**
 * @author 三刀
 * @version V1.0 , 2020/3/30
 */
class HttpUriDecoder extends AbstractDecoder {
    private static final ByteTree.EndMatcher URI_END_MATCHER = endByte -> endByte <= '?' && (endByte == ' ' || endByte == '?');
    private final HttpUriQueryDecoder uriQueryDecoder = new HttpUriQueryDecoder(getConfiguration());
    private final HttpProtocolDecoder protocolDecoder = new HttpProtocolDecoder(getConfiguration());

    public HttpUriDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Decoder decode0(ByteBuffer byteBuffer, Request request) {
        ByteTree<ServerHandler<?, ?>> uriTreeNode = StringUtils.scanByteTree(byteBuffer, URI_END_MATCHER, getConfiguration().getUriByteTree());
        if (uriTreeNode != null) {
            request.setUri(uriTreeNode.getStringValue());
            if (uriTreeNode.getAttach() == null) {
                request.setServerHandler(request.getConfiguration().getHttpServerHandler());
            } else {
                request.setServerHandler(uriTreeNode.getAttach());
            }

            switch (byteBuffer.get(byteBuffer.position() - 1)) {
                case Constant.SP:
                    return protocolDecoder.decode(byteBuffer, request);
                case '?':
                    return uriQueryDecoder.decode(byteBuffer, request);
                default:
                    throw new HttpException(HttpStatus.BAD_REQUEST);
            }
        } else {
            return this;
        }
    }

}
