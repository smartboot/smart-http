/*
 * Copyright (c) 2018, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: Consts.java
 * Date: 2018-02-06
 * Author: sandao
 */

package org.smartboot.http.utils;

import org.smartboot.http.server.Http11Request;
import org.smartboot.socket.util.AttachKey;

import java.nio.charset.Charset;

public interface Consts {

    String SCHEMA_HTTP = "http";
    String SCHEMA_HTTPS = "https";
    /**
     * Horizontal space
     */
    public static final byte SP = 32;

    /**
     * Horizontal tab
     */
    public static final byte HT = 9;

    /**
     * Carriage return
     */
    public static final byte CR = 13;

    /**
     * Equals '='
     */
    public static final byte EQUALS = 61;

    /**
     * Line feed character
     */
    public static final byte LF = 10;

    /**
     * Colon ':'
     */
    public static final byte COLON = 58;

    /**
     * Semicolon ';'
     */
    public static final byte SEMICOLON = 59;

    /**
     * Comma ','
     */
    public static final byte COMMA = 44;

    /**
     * Double quote '"'
     */
    public static final byte DOUBLE_QUOTE = '"';

    /**
     * Default character set (UTF-8)
     */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * Horizontal space
     */
    public static final char SP_CHAR = (char) SP;

    public static final byte[] CRLF = {Consts.CR, Consts.LF};

    byte[] COLON_ARRAY = {COLON};

    byte[] SP_ARRAY = {SP};

    AttachKey<Http11Request> ATTACH_KEY_REQUEST = AttachKey.valueOf("request");

    AttachKey<Thread> ATTACH_KEY_CURRENT_THREAD = AttachKey.valueOf("thread");

    AttachKey<byte[]> ATTACH_KEY_CACHE_BYTES = AttachKey.valueOf("cacheBytes");
}