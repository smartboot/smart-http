/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpStatus.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.enums;

import org.smartboot.http.common.utils.Constant;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;

public class HttpStatus {

    // 1xx Informational

    public static final HttpStatus CONTINUE = new HttpStatus(100, "Continue");
    public static final HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(101, "Switching Protocols");
    public static final HttpStatus PROCESSING = new HttpStatus(102, "Processing");
    public static final HttpStatus CHECKPOINT = new HttpStatus(103, "Checkpoint");

    // 2xx Success

    public static final HttpStatus OK = new HttpStatus(200, "OK", true);
    public static final HttpStatus CREATED = new HttpStatus(201, "Created");
    public static final HttpStatus ACCEPTED = new HttpStatus(202, "Accepted");
    public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = new HttpStatus(203, "Non-Authoritative Information");
    public static final HttpStatus NO_CONTENT = new HttpStatus(204, "No Content");
    public static final HttpStatus RESET_CONTENT = new HttpStatus(205, "Reset Content");
    public static final HttpStatus PARTIAL_CONTENT = new HttpStatus(206, "Partial Content");
    public static final HttpStatus MULTI_STATUS = new HttpStatus(207, "Multi-Status");
    public static final HttpStatus ALREADY_REPORTED = new HttpStatus(208, "Already Reported");
    public static final HttpStatus IM_USED = new HttpStatus(226, "IM Used");

    // 3xx Redirection

    public static final HttpStatus MULTIPLE_CHOICES = new HttpStatus(300, "Multiple Choices");
    public static final HttpStatus MOVED_PERMANENTLY = new HttpStatus(301, "Moved Permanently");
    public static final HttpStatus FOUND = new HttpStatus(302, "Found");
    public static final HttpStatus SEE_OTHER = new HttpStatus(303, "See Other");
    public static final HttpStatus NOT_MODIFIED = new HttpStatus(304, "Not Modified");
    public static final HttpStatus USE_PROXY = new HttpStatus(305, "Use Proxy");
    public static final HttpStatus TEMPORARY_REDIRECT = new HttpStatus(307, "Temporary Redirect");
    public static final HttpStatus PERMANENT_REDIRECT = new HttpStatus(308, "Permanent Redirect");

    // --- 4xx Client Error ---

    public static final HttpStatus BAD_REQUEST = new HttpStatus(400, "Bad Request");
    public static final HttpStatus UNAUTHORIZED = new HttpStatus(401, "Unauthorized");
    public static final HttpStatus PAYMENT_REQUIRED = new HttpStatus(402, "Payment Required");
    public static final HttpStatus FORBIDDEN = new HttpStatus(403, "Forbidden");
    public static final HttpStatus NOT_FOUND = new HttpStatus(404, "Not Found");
    public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(405, "Method Not Allowed");
    public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(406, "Not Acceptable");
    public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(407, "Proxy Authentication Required");
    public static final HttpStatus REQUEST_TIMEOUT = new HttpStatus(408, "Request Timeout");
    public static final HttpStatus CONFLICT = new HttpStatus(409, "Conflict");
    public static final HttpStatus GONE = new HttpStatus(410, "Gone");
    public static final HttpStatus LENGTH_REQUIRED = new HttpStatus(411, "Length Required");
    public static final HttpStatus PRECONDITION_FAILED = new HttpStatus(412, "Precondition Failed");
    public static final HttpStatus PAYLOAD_TOO_LARGE = new HttpStatus(413, "Payload Too Large");
    public static final HttpStatus URI_TOO_LONG = new HttpStatus(414, "URI Too Long");
    public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = new HttpStatus(415, "Unsupported Media Type");
    public static final HttpStatus REQUESTED_RANGE_NOT_SATISFIABLE = new HttpStatus(416, "Requested range not satisfiable");
    public static final HttpStatus EXPECTATION_FAILED = new HttpStatus(417, "Expectation Failed");
    public static final HttpStatus I_AM_A_TEAPOT = new HttpStatus(418, "I'm a teapot");
    public static final HttpStatus INSUFFICIENT_SPACE_ON_RESOURCE = new HttpStatus(419, "Insufficient Space On Resource");
    public static final HttpStatus METHOD_FAILURE = new HttpStatus(420, "Method Failure");
    public static final HttpStatus DESTINATION_LOCKED = new HttpStatus(421, "Destination Locked");
    public static final HttpStatus UNPROCESSABLE_ENTITY = new HttpStatus(422, "Unprocessable Entity");
    public static final HttpStatus LOCKED = new HttpStatus(423, "Locked");
    public static final HttpStatus FAILED_DEPENDENCY = new HttpStatus(424, "Failed Dependency");
    public static final HttpStatus UPGRADE_REQUIRED = new HttpStatus(426, "Upgrade Required");
    public static final HttpStatus PRECONDITION_REQUIRED = new HttpStatus(428, "Precondition Required");
    public static final HttpStatus TOO_MANY_REQUESTS = new HttpStatus(429, "Too Many Requests");
    public static final HttpStatus REQUEST_HEADER_FIELDS_TOO_LARGE = new HttpStatus(431, "Request Header Fields Too Large");

    // --- 5xx Server Error ---

    public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(500, "Internal Server Error");
    public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(501, "Not Implemented");
    public static final HttpStatus BAD_GATEWAY = new HttpStatus(502, "Bad Gateway");
    public static final HttpStatus SERVICE_UNAVAILABLE = new HttpStatus(503, "Service Unavailable");
    public static final HttpStatus GATEWAY_TIMEOUT = new HttpStatus(504, "Gateway Timeout");
    public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(505, "HTTP Version not supported");
    public static final HttpStatus VARIANT_ALSO_NEGOTIATES = new HttpStatus(506, "Variant Also Negotiates");
    public static final HttpStatus INSUFFICIENT_STORAGE = new HttpStatus(507, "Insufficient Storage");
    public static final HttpStatus LOOP_DETECTED = new HttpStatus(508, "Loop Detected");
    public static final HttpStatus BANDWIDTH_LIMIT_EXCEEDED = new HttpStatus(509, "Bandwidth Limit Exceeded");
    public static final HttpStatus NOT_EXTENDED = new HttpStatus(510, "Not Extended");
    public static final HttpStatus NETWORK_AUTHENTICATION_REQUIRED = new HttpStatus(511, "Network Authentication Required");

    private final int value;

    private final String reasonPhrase;

    private final byte[] bytes;

    public HttpStatus(int value, String reasonPhrase, boolean cache) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
        if (cache) {
            this.bytes = (value + " " + reasonPhrase + "\r\n").getBytes();
        } else {
            this.bytes = null;
        }

    }

    public HttpStatus(int value, String reasonPhrase) {
        this(value, reasonPhrase, false);
    }

    /**
     * Return the integer value of this status code.
     */
    public int value() {
        return this.value;
    }


    /**
     * Return the reason phrase of this status code.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public static HttpStatus valueOf(int value) {
        if (value >= 100 && value <= 103) {
            switch (value) {
                case 100:
                    return HttpStatus.CONTINUE;
                case 101:
                    return HttpStatus.SWITCHING_PROTOCOLS;
                case 102:
                    return HttpStatus.PROCESSING;
                case 103:
                    return HttpStatus.CHECKPOINT;
            }
        } else if (value >= 200 && value <= 208) {
            switch (value) {
                case 200:
                    return HttpStatus.OK;
                case 201:
                    return HttpStatus.CREATED;
                case 202:
                    return HttpStatus.ACCEPTED;
                case 203:
                    return HttpStatus.NON_AUTHORITATIVE_INFORMATION;
                case 204:
                    return HttpStatus.NO_CONTENT;
                case 205:
                    return HttpStatus.RESET_CONTENT;
                case 206:
                    return HttpStatus.PARTIAL_CONTENT;
                case 207:
                    return HttpStatus.MULTI_STATUS;
                case 208:
                    return HttpStatus.ALREADY_REPORTED;
            }
        } else if (value >= 300 && value <= 303) {
            return HttpStatus.MULTIPLE_CHOICES;
        } else if (value >= 400 && value <= 403) {
            switch (value) {
                case 400:
                    return HttpStatus.BAD_REQUEST;
                case 401:
                    return HttpStatus.UNAUTHORIZED;
                case 402:
                    return HttpStatus.PAYMENT_REQUIRED;
                case 403:
                    return HttpStatus.FORBIDDEN;
            }
            return HttpStatus.BAD_REQUEST;
        } else if (value >= 500 && value <= 503) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return null;
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    public void write(WriteBuffer writeBuffer) throws IOException {
        if (bytes == null) {
            writeBuffer.writeByte((byte) (value / 100 + '0'));
            writeBuffer.writeByte((byte) (value / 10 % 10 + '0'));
            writeBuffer.writeByte((byte) (value % 10 + '0'));
            writeBuffer.writeByte((byte) ' ');
            writeBuffer.write(reasonPhrase.getBytes());
            writeBuffer.write(Constant.CRLF_BYTES);
        } else {
            writeBuffer.write(bytes);
        }
    }
}
