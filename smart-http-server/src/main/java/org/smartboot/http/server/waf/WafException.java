package org.smartboot.http.server.waf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;

public class WafException extends HttpException {
    public WafException(HttpStatus httpStatus) {
        super(httpStatus, WafConfiguration.DESC);
    }

    public WafException(HttpStatus httpStatus, String desc) {
        super(httpStatus, desc);
    }
}
