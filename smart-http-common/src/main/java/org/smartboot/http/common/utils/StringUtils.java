/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: StringUtils.java
 * Date: 2020-01-01
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/
package org.smartboot.http.common.utils;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {

    /**
     * An empty immutable {@code String} array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    /**
     * The empty String {@code ""}.
     *
     * @since 2.0
     */
    public static final String EMPTY = "";


    /**
     * Represents a failed index search.
     *
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;
    public static final List<StringCache>[] String_CACHE_EMPTY = new List[0];
    public static final List<StringCache>[] String_CACHE_COMMON = new List[128];
    public static final List<StringCache>[] String_CACHE_HEADER_NAME = new List[32];
    public static final List<StringCache>[] String_CACHE_URI = new List[64];
    public static final List<IntegerCache>[] INTEGER_CACHE_HTTP_STATUS_CODE = new List[8];
    private static final int CHAR_CACHE_MAX_LENGTH = 1024;
    private static final ThreadLocal<char[]> charsCacheThreadLocal = ThreadLocal.withInitial(() -> new char[CHAR_CACHE_MAX_LENGTH]);

//    public static String convertToString(byte[] bytes, int length, List<StringCache>[] cacheList) {
//        return convertToString(bytes, 0, length, cacheList);
//    }

    static {
        for (int i = 0; i < INTEGER_CACHE_HTTP_STATUS_CODE.length; i++) {
            INTEGER_CACHE_HTTP_STATUS_CODE[i] = new ArrayList<>(8);
        }

        for (int i = 0; i < String_CACHE_COMMON.length; i++) {
            String_CACHE_COMMON[i] = new ArrayList<>(8);
        }
        for (int i = 0; i < String_CACHE_HEADER_NAME.length; i++) {
            String_CACHE_HEADER_NAME[i] = new ArrayList<>(8);
        }
        for (int i = 0; i < String_CACHE_URI.length; i++) {
            String_CACHE_URI[i] = new ArrayList<>(8);
        }
        for (HttpMethodEnum httpMethodEnum : HttpMethodEnum.values()) {
            ByteTree.ROOT.addNode(httpMethodEnum.getMethod() + " ");
        }
        for (HeaderNameEnum headerNameEnum : HeaderNameEnum.values()) {
            addCache(String_CACHE_HEADER_NAME, headerNameEnum.getName());
        }
        for (HeaderValueEnum headerNameEnum : HeaderValueEnum.values()) {
            addCache(String_CACHE_COMMON, headerNameEnum.getName());
        }
        for (HttpProtocolEnum httpProtocolEnum : HttpProtocolEnum.values()) {
            ByteTree.ROOT.addNode(httpProtocolEnum.getProtocol() + "\r\n");
        }

        ByteTree.ROOT.addNode("/plaintext ");
        ByteTree.ROOT.addNode("/json ");
    }

    public StringUtils() {
        super();
    }

    public static String trim(final String str) {
        return str == null ? null : str.trim();
    }

    public static int convertToInteger(ByteBuffer buffer, int offset, int length, List<IntegerCache>[] cacheList) {
        offset = buffer.arrayOffset() + offset;
        byte[] bytes = buffer.array();
        if (length >= cacheList.length) {
            return Integer.parseInt(new String(bytes, offset, length, StandardCharsets.US_ASCII));
        }
        List<IntegerCache> list = cacheList[length];
        for (int i = list.size() - 1; i > -1; i--) {
            IntegerCache cache = list.get(i);
            if (equals(cache.bytes, bytes, offset)) {
                return cache.value;
            }
        }
        synchronized (list) {
            for (IntegerCache cache : list) {
                if (equals(cache.bytes, bytes, offset)) {
                    return cache.value;
                }
            }
            String str = new String(bytes, offset, length, StandardCharsets.US_ASCII);
            list.add(new IntegerCache(str.getBytes(), Integer.parseInt(str)));
            return Integer.parseInt(str);
        }
    }

    public static String convertToString(ByteBuffer buffer, int offset, int length) {
        return convertToString(buffer, offset, length, String_CACHE_EMPTY);
    }

    public static String convertToString(ByteBuffer buffer, int offset, int length, List<StringCache>[] cacheList) {
        return convertToString(buffer, offset, length, cacheList, false);
    }

    public static String convertToString(ByteBuffer buffer, int offset, int length, List<StringCache>[] cacheList, boolean readonly) {
        if (length == 0) {
            return "";
        }
        offset = buffer.arrayOffset() + offset;
        byte[] bytes = buffer.array();
        if (length >= cacheList.length) {
//            System.out.println(new String(bytes, offset, length));
            return newAsciiString(bytes, offset, length);
        }
        List<StringCache> list = cacheList[length];
        for (int i = list.size() - 1; i > -1; i--) {
            StringCache cache = list.get(i);
            if (equals(cache.bytes, bytes, offset)) {
                return cache.value;
            }
        }
        if (readonly) {
//            System.out.println(new String(bytes, offset, length));
            return newAsciiString(bytes, offset, length);
        }
        synchronized (list) {
            for (StringCache cache : list) {
                if (equals(cache.bytes, bytes, offset)) {
                    return cache.value;
                }
            }
            String str = newAsciiString(bytes, offset, length);
            list.add(new StringCache(str.getBytes(), str));
            return str;
        }
    }

    private static boolean equals(byte[] b0, byte[] b1, int offset) {
        for (int i = b0.length - 1; i > 0; i--) {
            if (b0[i] != b1[i + offset]) {
                return false;
            }
        }
        return b0[0] == b1[offset];
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !StringUtils.isBlank(cs);
    }

    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        return regionMatches(cs1, false, 0, cs2, 0, Math.max(cs1.length(), cs2.length()));
    }

    private static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,
                                         final CharSequence substring, final int start, final int length) {
        if (cs instanceof String && substring instanceof String) {
            return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
        } else {
            int index1 = thisStart;
            int index2 = start;
            int tmpLen = length;

            while (tmpLen-- > 0) {
                char c1 = cs.charAt(index1++);
                char c2 = substring.charAt(index2++);

                if (c1 == c2) {
                    continue;
                }

                if (!ignoreCase) {
                    return false;
                }

                // The same check as in String.regionMatches():
                if (Character.toUpperCase(c1) != Character.toUpperCase(c2)
                        && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                    return false;
                }
            }

            return true;
        }
    }

    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }

        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return EMPTY;
        }

        return str.substring(start);
    }

    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        if (end > str.length()) {
            end = str.length();
        }

        if (start > end) {
            return EMPTY;
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    public static String substringBefore(final String str, final String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.isEmpty()) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String substringAfter(final String str, final String separator) {
        if (isEmpty(str)) {
            return str;
        }
        if (separator == null) {
            return EMPTY;
        }
        final int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    public static String[] split(final String str, final String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }

    public static String[] splitPreserveAllTokens(final String str, final String separatorChars) {
        return splitWorker(str, separatorChars, -1, true);
    }

    private static String[] splitWorker(final String str, final String separatorChars, final int max, final boolean preserveAllTokens) {

        if (str == null) {
            return null;
        }
        final int len = str.length();
        if (len == 0) {
            return EMPTY_STRING_ARRAY;
        }
        final List<String> list = new ArrayList<String>();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            final char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || preserveAllTokens && lastMatch) {
            list.add(str.substring(start, i));
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] tokenizeToStringArray(
            String str, String delimiters, boolean trimTokens, boolean ignoreEmptyTokens) {

        if (str == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(str, delimiters);
        List<String> tokens = new ArrayList<String>();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (trimTokens) {
                token = token.trim();
            }
            if (!ignoreEmptyTokens || token.length() > 0) {
                tokens.add(token);
            }
        }
        return toStringArray(tokens);
    }

    public static String[] toStringArray(Collection<String> collection) {
        if (collection == null) {
            return null;
        }
        return collection.toArray(new String[collection.size()]);
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static boolean startsWith(final CharSequence str, final CharSequence prefix) {
        return startsWith(str, prefix, false);
    }

    private static boolean startsWith(final CharSequence str, final CharSequence prefix, final boolean ignoreCase) {
        if (str == null || prefix == null) {
            return str == null && prefix == null;
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return regionMatches(str, ignoreCase, 0, prefix, 0, prefix.length());
    }

    public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
        return endsWith(str, suffix, false);
    }

    private static boolean endsWith(final CharSequence str, final CharSequence suffix, final boolean ignoreCase) {
        if (str == null || suffix == null) {
            return str == null && suffix == null;
        }
        if (suffix.length() > str.length()) {
            return false;
        }
        final int strOffset = str.length() - suffix.length();
        return regionMatches(str, ignoreCase, strOffset, suffix, 0, suffix.length());
    }

    public static String searchFromByteTree(ByteBuffer buffer, byte[] split) {
        trimBuffer(buffer);
        int position = buffer.position() + buffer.arrayOffset();
        int limit = buffer.limit() + buffer.arrayOffset();
        byte[] data = buffer.array();
        ByteTree byteTree = ByteTree.ROOT.search(data, position, limit, split, false);
        if (byteTree == null) {
            return null;
        }
        buffer.position(buffer.position() + byteTree.getDepth() + 1);
        return byteTree.getStringValue();
    }

    public static int scanUntilAndTrim(ByteBuffer buffer, byte split) {
        trimBuffer(buffer);
        int position = buffer.position() + buffer.arrayOffset();
        int limit = buffer.limit() + buffer.arrayOffset();
        byte[] data = buffer.array();
        while (position < limit) {
            if (data[position++] == split) {
                int length = position - buffer.arrayOffset() - buffer.position() - 1;
                buffer.position(position - buffer.arrayOffset());
                return length;
            }
        }
        return -1;
    }

    public static int scanCRLFAndTrim(ByteBuffer buffer) {
        trimBuffer(buffer);
        int position = buffer.position() + buffer.arrayOffset();
        int limit = buffer.limit() + buffer.arrayOffset();
        byte[] data = buffer.array();

        while (limit - position >= 2) {
            byte b = data[position + 1];
            if (b == Constant.LF) {
                if (data[position] == Constant.CR) {
                    int length = position - buffer.arrayOffset() - buffer.position() + 1;
                    buffer.position(position - buffer.arrayOffset() + 2);
                    return length;
                }
                throw new IllegalStateException();
            } else if (b == Constant.CR) {
                position += 1;
            } else {
                position += 2;
            }
        }
        return -1;
    }

    public static void trimBuffer(ByteBuffer buffer) {
        while (buffer.hasRemaining() && buffer.get(buffer.position()) == Constant.SP) {
            buffer.position(buffer.position() + 1);
        }
    }

    public static boolean addCache(List<StringCache>[] cache, String str) {
        byte[] bytes = str.getBytes();
        if (bytes.length >= cache.length) {
            return false;
        }
        cache[bytes.length].add(new StringCache(bytes, str));
        return true;
    }

    private static String newAsciiString(byte[] bytes, int offset, int len) {
        char[] chars;
        if (len <= CHAR_CACHE_MAX_LENGTH) {
            chars = charsCacheThreadLocal.get();
        } else {
            chars = new char[len];
        }
        for (int i = 0; i < len; i++) {
            chars[i] = (char) bytes[offset++];
        }
        return new String(chars, 0, len);
    }

    static class StringCache {
        final byte[] bytes;
        final String value;

        public StringCache(byte[] bytes, String value) {
            this.bytes = bytes;
            this.value = value;
        }
    }

    static class IntegerCache {
        final byte[] bytes;
        final int value;

        public IntegerCache(byte[] bytes, int value) {
            this.bytes = bytes;
            this.value = value;
        }
    }
}
