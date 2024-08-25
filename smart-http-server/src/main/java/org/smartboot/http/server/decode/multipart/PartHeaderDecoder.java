package org.smartboot.http.server.decode.multipart;

import org.smartboot.http.common.HeaderValue;
import org.smartboot.http.common.Multipart;
import org.smartboot.http.common.Part;
import org.smartboot.http.common.utils.ParamDecodeUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.AbstractDecoder;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.impl.Request;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @Description: TODO
 * @Author MiSinG
 * @Date 2024/6/26
 * @Version V1.0
 **/
public class PartHeaderDecoder extends AbstractDecoder {

    private static volatile PartHeaderDecoder INSTANCE;
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String NAME = "name";
    public static final String FILENAME = "filename";

    public PartHeaderDecoder(HttpServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected Decoder decode0(ByteBuffer byteBuffer, Request request) {
        Multipart multipart = request.getMultipart();
        String header = readHeader(byteBuffer,multipart);
        if (header != null) {
            Part part = new Part();
            List<HeaderValue> parsedHeaders = getParsedHeaders(header);
            part.setHeaders(parsedHeaders);
            fillHeaders(parsedHeaders, part);
//            request.setPart(part);
            return PartBodyDecoder.getInstance(getConfiguration()).decode(byteBuffer, request);
        }
        return this;
    }

    public String readHeader(ByteBuffer byteBuffer, Multipart multipart) {
        int bufferPos = multipart.getBufferPos();
        int pos = 0;
        int start = byteBuffer.position();

        while (byteBuffer.hasRemaining()) {
            byte b = byteBuffer.get();

            if (b == Multipart.HEADER_SEPARATOR[pos]) {
                pos++;
                if (pos == Multipart.HEADER_SEPARATOR.length) {
                    //将buffer中的数据转为string输出
                    String headers;
                    headers = new String(byteBuffer.array(), start, byteBuffer.position() - start, StandardCharsets.UTF_8);
                    multipart.setBufferPos(bufferPos);
                    return headers;
                }
            } else {
                pos = 0;
            }
        }
        return null;
    }

    public List<HeaderValue> getParsedHeaders(String headerPart) {
        int length = headerPart.length();
        List<HeaderValue> headers = new ArrayList<>();
        int start = 0;

        while (start < length) {
            int end = getIndexEndOfLine(headerPart, start);

            // 空行标志着头部结束
            if (start == end) {
                break;
            }

            StringBuilder header = new StringBuilder(headerPart.substring(start, end));
            start = end + 2;

            // 合并可能换行的头部字段
            while (start < length) {
                int notEmptyStart = start;

                // 跳过空白字符
                while (notEmptyStart < length) {
                    char c = headerPart.charAt(notEmptyStart);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                    notEmptyStart++;
                }

                // 如果没有找到空白字符，则继续处理下一个头部
                if (notEmptyStart == start) {
                    break;
                }

                end = getIndexEndOfLine(headerPart, notEmptyStart);
                header.append(' ').append(headerPart, notEmptyStart, end);
                start = end + 2;
            }

            parseHeaderLine(headers, header.toString());
        }

        return headers;
    }


    private int getIndexEndOfLine(String headerPart, int end) {
        int index = end;
        while (true) {
            int offset = headerPart.indexOf('\r', index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException("The header is incorrectly formatted");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }

    private void parseHeaderLine(List<HeaderValue> headers, String headerStr) {
        int colonOffset = headerStr.indexOf(':');
        if (colonOffset == -1) {
            // 不符合则跳过
            return;
        }
        String headerName = headerStr.substring(0, colonOffset).trim();
        String headerValue = headerStr.substring(colonOffset + 1).trim();
        HeaderValue header = new HeaderValue(headerName, headerValue);
        for (HeaderValue h : headers) {
            if (h.getName().equals(headerName)) {
                // 如果找到具有相同名称的 HeaderValue，设置新的 HeaderValue 作为其下一个值
                h.setNextValue(header);
                return;
            }
        }
        // 没有相同的就添加新的头部
        headers.add(header);
    }

    public void fillHeaders(List<HeaderValue> parsedHeaders, Part part) {
        for (HeaderValue headerValue : parsedHeaders) {
            if (headerValue.getName().equals(CONTENT_DISPOSITION)) {
                Map<String, String> parsed = parse(headerValue.getValue(), ';');
                part.setName(StringUtils.isEmpty(parsed.get(NAME))?StringUtils.EMPTY:parsed.get(NAME));

                String fileName;
                if (parsed.containsKey(FILENAME)) {
                    part.setIsFile(true);
                    fileName = parsed.get(FILENAME);
                    if (fileName != null) {
                        fileName = fileName.trim();
                    } else {
                        fileName = StringUtils.EMPTY;
                    }
                    part.setFileName(fileName);
                }
            } else if (headerValue.getName().equals(CONTENT_TYPE)) {
                part.setContentType(headerValue.getValue());
            }
        }
    }

    public Map<String, String> parse(String str, char separator) {
        if (str == null) {
            return new HashMap<>();
        }
        return parse(str.toCharArray(),0,str.length(), separator);
    }

    public Map<String, String> parse(char[] charArray, int offset, int length, char separator) {

        if (charArray == null) {
            return new HashMap<>();
        }
        Map<String, String> params = new HashMap<>();
        int pos = offset;

        while (pos < length) {
            // 取paramName
            String paramName;
            String paramValue = null;
            StringBuilder paramNameBuilder = new StringBuilder();
            char[] delimiters = {'=', separator};
            boolean foundDelimiter = false;

            // 遍历字符数组直到找到分隔符或达到长度
            while (pos < length && !foundDelimiter) {
                char currentChar = charArray[pos];

                // 检查当前字符是否为分隔符
                for (char delimiter : delimiters) {
                    if (currentChar == delimiter) {
                        foundDelimiter = true;
                        break;
                    }
                }

                if (!foundDelimiter) {
                    paramNameBuilder.append(currentChar);
                    pos++;
                }
            }
            // 返回去除前后空格的结果
            paramName =  paramNameBuilder.toString().trim();

            if (pos < length && charArray[pos] == '=') {
                pos++; // 跳过 '='
                // 取paramValue
                StringBuilder paramValueBuilder = new StringBuilder();
                boolean insideQuotes = false;

                // 检查是否处于引号内，并解析字符
                while (pos < length) {
                    char currentChar = charArray[pos++];

                    if (currentChar == '\"') {
                        // 切换引号状态
                        insideQuotes = !insideQuotes;
                        continue;
                    }

                    if (!insideQuotes) {
                        // 非引号内的字符，检查是否为分隔符
                        for (char delimiter : delimiters) {
                            if (currentChar == delimiter) {
                                // 遇到分隔符，返回解析结果
                                paramValue = paramValueBuilder.toString().trim();
                                break;
                            }
                        }
                        // 退出外部while循环
                        if (null != paramValue) break;
                    }

                    paramValueBuilder.append(currentChar);
                }

                if (paramValue == null) {
                    // 如果没有遇到分隔符，处理到字符串结尾
                    paramValue = paramValueBuilder.toString().trim();
                }

                try {
                    paramValue = ParamDecodeUtils.hasEncodedValue(paramName) ?
                            ParamDecodeUtils.decodeRFC2231Text(paramValue) : ParamDecodeUtils.decodeRFC2047Text(paramValue);
                } catch (UnsupportedEncodingException e) {
                    //
                }
            }
            if (pos < length && charArray[pos] == separator) {
                pos++; // 跳过 分隔符
            }
            if (!StringUtils.isEmpty(paramName)) {
                paramName = ParamDecodeUtils.stripDelimiter(paramName);
                params.put(paramName, paramValue);
            }
        }
        return params;
    }

    public static PartHeaderDecoder getInstance(HttpServerConfiguration configuration) {
        if (INSTANCE == null) {
            synchronized (PartHeaderDecoder.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PartHeaderDecoder(configuration);
                }
            }
        }
        return INSTANCE;
    }
}
