package org.smartboot.http.server.v2;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.handle.RouteHandle;
import org.smartboot.http.server.handle.http11.RFC2612RequestHandle;
import org.smartboot.http.server.handle.http11.ResponseHandle;
import org.smartboot.http.server.http11.DefaultHttpResponse;
import org.smartboot.http.server.v2.decode.Http11Request;
import org.smartboot.http.server.v2.decode.HttpRequestProtocol;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Http11Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageProcessor.class);
    private static byte[] b = ("HTTP/1.1 200 OK\r\n" +
            "Server:smart-socket\r\n" +
            "Connection:keep-alive\r\n" +
            "Host:localhost\r\n" +
            "Content-Length:31\r\n" +
            "Date:Wed, 11 Apr 2018 12:35:01 GMT\r\n\r\n" +
            "Hello smart-socket http server!").getBytes();
    private ThreadLocal<DefaultHttpResponse> RESPONSE_THREAD_LOCAL = null;
    /**
     * Http消息处理器
     */
    private HttpHandle processHandle;

    private RouteHandle routeHandle;

    private ResponseHandle responseHandle;

    public HttpMessageProcessor(String baseDir) {
        processHandle = new RFC2612RequestHandle();
        routeHandle = new RouteHandle(baseDir);
        processHandle.next(routeHandle);

        responseHandle = new ResponseHandle();
        RESPONSE_THREAD_LOCAL = new ThreadLocal<DefaultHttpResponse>() {
            @Override
            protected DefaultHttpResponse initialValue() {
                return new DefaultHttpResponse(responseHandle);
            }
        };
    }

    public static void main(String[] args) {
        HttpMessageProcessor processor = new HttpMessageProcessor("./");
        processor.route("/", new HttpHandle() {
            byte[] body = "welcome to smart-socket http server!".getBytes();

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {

                response.setHeader(HttpHeaderConstant.Names.CONTENT_LENGTH, body.length + "");
                response.getOutputStream().write(body);
            }
        });
        AioQuickServer<Http11Request> server = new AioQuickServer<Http11Request>(8080, new HttpRequestProtocol(), processor);
        server.setWriteQueueSize(1024);
        server.setReadBufferSize(256);
        server.setFaster(true);
//        server.setAsyncProcess(true);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(AioSession<Http11Request> session, Http11Request request) {
        try {
//            if (true) {
//                ByteBuffer buffer = DirectBufferUtil.getTemporaryDirectBuffer(b.length).put(b);
//                buffer.flip();
//                session.write(buffer);
//                request.rest();
//                return;
//            }
//            System.out.println(request);
            DefaultHttpResponse httpResponse = RESPONSE_THREAD_LOCAL.get();
            httpResponse.init(session, request);
            try {
                processHandle.doHandle(request, httpResponse);
            } catch (HttpException e) {
                httpResponse.setHttpStatus(HttpStatus.valueOf(e.getHttpCode()));
                httpResponse.getOutputStream().write(e.getDesc().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                httpResponse.getOutputStream().write(e.fillInStackTrace().toString().getBytes());
            }

            httpResponse.getOutputStream().close();


            if (!StringUtils.equalsIgnoreCase(HttpHeaderConstant.Values.KEEPALIVE, request.getHeader(HttpHeaderConstant.Names.CONNECTION)) || httpResponse.getHttpStatus() != HttpStatus.OK) {
                System.out.println(httpResponse.getHttpStatus() + " " + request);
                session.close(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stateEvent(AioSession<Http11Request> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
//        if (throwable != null) {
//            throwable.printStackTrace();
//        }
        switch (stateMachineEnum) {
            case NEW_SESSION:
                session.setAttachment(new ThreadLocal<Http11Request>() {
                    @Override
                    protected Http11Request initialValue() {
                        return new Http11Request();
                    }
                });
                break;
            case FLOW_LIMIT:
//                LOGGER.warn("流控");
                break;
            case RELEASE_FLOW_LIMIT:
//                LOGGER.warn("释放流控");
                break;
            case PROCESS_EXCEPTION:
                session.close();
                break;
        }
    }

    public void route(String urlPattern, HttpHandle httpHandle) {
        routeHandle.route(urlPattern, httpHandle);
    }
}
