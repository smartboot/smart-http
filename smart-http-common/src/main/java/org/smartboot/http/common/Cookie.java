/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Cookie.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common;

import org.smartboot.http.common.utils.DateUtils;
import org.smartboot.http.common.utils.StringUtils;

import java.util.Date;

/**
 * @author 三刀
 * @version V1.0 , 2018/8/31
 */
public class Cookie {
    public static final String DOMAIN = "$Domain";
    public static final String VERSION = "$Version";
    public static final String PATH = "$Path";

    private final String name;
    private String value;
    private String path;
    private String domain;
    private int maxAge = -1;
    private Date expires;
    private boolean secure;
    private boolean httpOnly;
    private int version = 0;
    private String comment;


    public Cookie(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public Cookie(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Cookie setValue(final String value) {
        this.value = value;
        return this;
    }

    public String getPath() {
        return path;
    }

    public Cookie setPath(final String path) {
        this.path = path;
        return this;
    }

    public String getDomain() {
        return domain;
    }

    public Cookie setDomain(final String domain) {
        this.domain = domain;
        return this;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public Cookie setMaxAge(final Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public boolean isSecure() {
        return secure;
    }

    public Cookie setSecure(final boolean secure) {
        this.secure = secure;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public Cookie setVersion(final int version) {
        this.version = version;
        return this;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public Cookie setHttpOnly(final boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }

    public Date getExpires() {
        return expires;
    }

    public Cookie setExpires(final Date expires) {
        this.expires = expires;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Cookie setComment(final String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append('=').append(getValue());
        if (StringUtils.isNotBlank(getPath())) {
            sb.append("; Path=").append(getPath());
        }
        if (StringUtils.isNotBlank(this.domain)) {
            sb.append("; Domain=").append(this.domain);
        }
        if (this.expires != null) {
            sb.append("; Expires=");
            sb.append(DateUtils.formatCookieExpire(expires));
        } else if (this.maxAge >= 0) {
            sb.append("; Max-Age=").append(this.maxAge);
            Date expires = new Date();
            expires.setTime(expires.getTime() + maxAge * 1000L);
            sb.append("; Expires=");
            sb.append(DateUtils.formatCookieExpire(expires));
        }

        if (this.secure) {
            sb.append("; Secure");
        }
        if (this.httpOnly) {
            sb.append("; HttpOnly");
        }
        return sb.toString();
    }
}
