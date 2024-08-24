package org.smartboot.http.test.server;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.multipart.PartHeaderDecoder;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ParamParserTest {

    PartHeaderDecoder decoder = PartHeaderDecoder.getInstance(new HttpServerConfiguration());

    @Test
    public void testBasic() {
        Map<String, String> parse = decoder.parse("Content-Disposition: form-data; name=\"largeFile\"; filename=\"largefile.txt\"\r\n", ';');
        assertEquals("{\"filename\":\"largefile.txt\",\"name\":\"largeFile\"}", JSON.toJSONString(parse));
    }

    @Test
    public void testEncodedParameter() {

        String s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= =?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n";
        Map<String, String> params = decoder.parse(s, ';');
        assertEquals("If you can read this you understand the example.", params.get("filename"));

        s = "Content-Disposition: form-data; name=\"field*\"; filename*=\"us-ascii'en-us'This%20is%20%2A%2A%2Afun%2A%2A%2A\"\r\n";
        params = decoder.parse(s, ';');
        assertEquals("This is ***fun***", params.get("filename"));

        s = "Content-Disposition: form-data; " + "name=\"file\"; filename*=UTF-8''%E3%81%93%E3%82%93%E3%81%AB%E3%81%A1%E3%81%AF\r\n";
        params = decoder.parse(s, ';');
        assertEquals("\u3053\u3093\u306B\u3061\u306F", params.get("filename")); // filename = "こんにちは"

        s = "Content-Disposition: form-data; name=\"file\"; filename*=UTF-8''%70%C3%A2%74%C3%A9\r\n";
        params = decoder.parse(s, ';');
        assertEquals("\u0070\u00e2\u0074\u00e9", params.get("filename")); // filename = "pâté"

        s = "Content-Disposition: form-data; name=\"file\"; file*name=UTF-8''%61%62%63\r\n";
        params = decoder.parse(s, ';');
        assertEquals("UTF-8''%61%62%63", params.get("file*name"));

        s = "Content-Disposition: form-data; name=\"file\"; filename*=a'bc\r\n";
        params = decoder.parse(s, ';');
        assertEquals("a'bc", params.get("filename"));

        s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= =?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n";
        params = decoder.parse(s, ';');
        assertEquals("If you can read this you understand the example.", params.get("filename"));

        s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?=\t  \r\n   =?ISO-8859-" + "2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n";
        params = decoder.parse(s, ';');
        assertEquals("If you can read this you understand the example.", params.get("filename"));

        s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?UTF-8?B?IGjDqSEgw6DDqMO0dSAhISE=?=\"\r\n";
        params = decoder.parse(s, ';');
        assertEquals(" h\u00e9! \u00e0\u00e8\u00f4u !!!", params.get("filename"));

        s = "Content-Disposition: form-data; name=\"file\"; filename=\"=?UTF-8?Q?_h=C3=A9!_=C3=A0=C3=A8=C3=B4u_!!!?=\"\r\n";
        params = decoder.parse(s, ';');
        assertEquals(" h\u00e9! \u00e0\u00e8\u00f4u !!!", params.get("filename"));

    }

}
