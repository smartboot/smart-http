package org.smartboot.http.test;

import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.decode.AbstractDecoder;
import org.smartboot.http.server.decode.Decoder;
import org.smartboot.http.server.decode.HttpMethodDecoder;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.http.server.impl.Request;

import java.nio.ByteBuffer;

@State(Scope.Thread)
public class MyBenchmark {
    private ByteBuffer buffer = ByteBuffer.wrap("GET /plaintext HTTP/1.1\r\nHOST: 127.0.0.1\r\nHOSTX: 127.0.0.1\r\n\r\n".getBytes());
    HttpServerConfiguration configuration = new HttpServerConfiguration();
//    Request request = new Request(configuration, null);
    Request request = null;
    AbstractDecoder httpMethodDecoder = new HttpMethodDecoder(configuration);

    @Benchmark
    @Test
    public void testMethod() {
        // 在这里编写要进行基准测试的代码
        buffer.position(0);
        Decoder decoder = httpMethodDecoder.decode(buffer, request);
        Assert.assertSame(decoder, HttpRequestProtocol.BODY_READY_DECODER);
        request.reset();
    }
}