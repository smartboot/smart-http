/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpUtils.java
 * Date: 2020-11-22
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import org.smartboot.http.common.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/22
 */
public class HttpUtils {
    /**
     * 解码URI中的参数
     *
     * @param paramStr http参数字符串： aa=111&bb=222
     * @param paramMap 参数存放Map
     */
    public static void decodeParamString(String paramStr, Map<String, String[]> paramMap) {
        if (StringUtils.isBlank(paramStr)) {
            return;
        }
        String[] uriParamStrArray = StringUtils.split(paramStr, "&");
        for (String param : uriParamStrArray) {
            int index = param.indexOf("=");
            if (index == -1) {
                continue;
            }
            try {
                String key = StringUtils.substring(param, 0, index);
                String value = URLDecoder.decode(StringUtils.substring(param, index + 1), "utf8");
                String[] values = paramMap.get(key);
                if (values == null) {
                    paramMap.put(key, new String[]{value});
                } else {
                    String[] newValue = new String[values.length + 1];
                    System.arraycopy(values, 0, newValue, 0, values.length);
                    newValue[values.length] = value;
                    paramMap.put(key, newValue);
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseCookie(final String cookie, final Map<String, Cookie> parsedCookies) {
        parseCookie(cookie, parsedCookies, false, true);
    }

    private static void parseCookie(final String cookie, final Map<String, Cookie> parsedCookies, boolean allowEqualInValue, boolean commaIsSeperator) {
        int state = 0;
        String name = null;
        int start = 0;
        boolean containsEscapedQuotes = false;
        final Map<String, String> cookies = new HashMap<>();
        final Map<String, String> additional = new HashMap<>();
        for (int i = 0; i < cookie.length(); ++i) {
            char c = cookie.charAt(i);
            switch (state) {
                case 0: {
                    //eat leading whitespace
                    if (c == ' ' || c == '\t' || c == ';') {
                        start = i + 1;
                        break;
                    }
                    state = 1;
                    //fall through
                }
                case 1: {
                    //extract key
                    if (c == '=') {
                        name = cookie.substring(start, i);
                        start = i + 1;
                        state = 2;
                    } else if (c == ';' || (commaIsSeperator && c == ',')) {
                        if (name != null) {
                            createCookie(name, cookie.substring(start, i), cookies, additional);
                        }
                        state = 0;
                        start = i + 1;
                    }
                    break;
                }
                case 2: {
                    //extract value
                    if (c == ';' || (commaIsSeperator && c == ',')) {
                        createCookie(name, cookie.substring(start, i), cookies, additional);
                        state = 0;
                        start = i + 1;
                    } else if (c == '"' && start == i) { //only process the " if it is the first character
                        containsEscapedQuotes = false;
                        state = 3;
                        start = i + 1;
                    } else if (!allowEqualInValue && c == '=') {
                        createCookie(name, cookie.substring(start, i), cookies, additional);
                        state = 4;
                        start = i + 1;
                    }
                    break;
                }
                case 3: {
                    //extract quoted value
                    if (c == '"') {
                        createCookie(name, containsEscapedQuotes ? unescapeDoubleQuotes(cookie.substring(start, i)) : cookie.substring(start, i), cookies, additional);
                        state = 0;
                        start = i + 1;
                    }
                    // Skip the next double quote char '"' when it is escaped by backslash '\' (i.e. \") inside the quoted value
                    if (c == '\\' && (i + 1 < cookie.length()) && cookie.charAt(i + 1) == '"') {
                        // But..., do not skip at the following conditions
                        if (i + 2 == cookie.length()) { // Cookie: key="\" or Cookie: key="...\"
                            break;
                        }
                        if (i + 2 < cookie.length() && (cookie.charAt(i + 2) == ';'      // Cookie: key="\"; key2=...
                                || (commaIsSeperator && cookie.charAt(i + 2) == ','))) { // Cookie: key="\", key2=...
                            break;
                        }
                        // Skip the next double quote char ('"' behind '\') in the cookie value
                        i++;
                        containsEscapedQuotes = true;
                    }
                    break;
                }
                case 4: {
                    //skip value portion behind '='
                    if (c == ';' || (commaIsSeperator && c == ',')) {
                        state = 0;
                    }
                    start = i + 1;
                    break;
                }
            }
        }
        if (state == 2) {
            createCookie(name, cookie.substring(start), cookies, additional);
        }

        for (final Map.Entry<String, String> entry : cookies.entrySet()) {
            Cookie c = new Cookie(entry.getKey(), entry.getValue());
            String domain = additional.get(Cookie.DOMAIN);
            if (domain != null) {
                c.setDomain(domain);
            }
            String version = additional.get(Cookie.VERSION);
            if (version != null) {
                c.setVersion(Integer.parseInt(version));
            }
            String path = additional.get(Cookie.PATH);
            if (path != null) {
                c.setPath(path);
            }
            parsedCookies.put(c.getName(), c);
        }
    }

    private static void createCookie(final String name, final String value,
                                     final Map<String, String> cookies, final Map<String, String> additional) {
        if (!name.isEmpty() && name.charAt(0) == '$') {
            if (!additional.containsKey(name)) {
                additional.put(name, value);
            }
        } else {
            if (!cookies.containsKey(name)) {
                cookies.put(name, value);
            }
        }
    }

    private static String unescapeDoubleQuotes(final String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // Replace all escaped double quote (\") to double quote (")
        char[] tmp = new char[value.length()];
        int dest = 0;
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\' && (i + 1 < value.length()) && value.charAt(i + 1) == '"') {
                i++;
            }
            tmp[dest] = value.charAt(i);
            dest++;
        }
        return new String(tmp, 0, dest);
    }

}
