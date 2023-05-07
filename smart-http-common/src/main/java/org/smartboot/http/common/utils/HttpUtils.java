/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpUtils.java
 * Date: 2020-11-22
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

    public static String getString(ByteBuffer buffer, ByteTree.EndMatcher endMatcher) {
        int position = buffer.position() + buffer.arrayOffset();
        int limit = buffer.limit() + buffer.arrayOffset();
        byte[] data = buffer.array();
        for (int i = position; i < limit; i++) {
            if (endMatcher.match(data[i])) {
                String str = new String(data, position, i++ - position, StandardCharsets.US_ASCII);
                while (data[i] == Constant.SP) {
                    i++;
                }
                buffer.position(i - buffer.arrayOffset());
                return str;
            }
        }
        return null;
    }
}
