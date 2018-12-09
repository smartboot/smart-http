package org.smartboot.http.server;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.decode.Http11Request;
import org.smartboot.http.server.decode.HttpRequestProtocol;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.handle.RouteHandle;
import org.smartboot.http.server.handle.http11.RFC2612RequestHandle;
import org.smartboot.http.server.http11.DefaultHttpResponse;
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
            "Content-Length:12\r\n" +
            "Date:Wed, 11 Apr 2018 12:35:01 GMT\r\n\r\n" +
            "Hello World!").getBytes();
    private ThreadLocal<DefaultHttpResponse> RESPONSE_THREAD_LOCAL = null;
    /**
     * Http消息处理器
     */
    private HttpHandle processHandle;
    private RouteHandle routeHandle;

    public HttpMessageProcessor(String baseDir) {
        processHandle = new RFC2612RequestHandle();
        routeHandle = new RouteHandle(baseDir);
        processHandle.next(routeHandle);

        RESPONSE_THREAD_LOCAL = new ThreadLocal<DefaultHttpResponse>() {
            @Override
            protected DefaultHttpResponse initialValue() {
                return new DefaultHttpResponse();
            }
        };
    }

    public static void main(String[] args) {
        System.setProperty("smart-socket.server.pageSize", (1024 * 1024 * 5) + "");
//        System.setProperty("smart-socket.session.writeChunkSize", (4096*2)+"");
        HttpMessageProcessor processor = new HttpMessageProcessor("./");
        processor.route("/plaintext", new HttpHandle() {
            byte[] body = "Hello World!".getBytes();

            @Override
            public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                response.setContentLength(body.length);
                response.getOutputStream().write(body);
            }
        });
        AioQuickServer<Http11Request> server = new AioQuickServer<Http11Request>(8080, new HttpRequestProtocol(), processor);
        server.setReadBufferSize(1024 * 4);
//        server.setBannerEnabled(false);
//        server.setThreadNum(1);
//        server.setFairIO(true);
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
//                session.getOutputStream().write(b);
//                request.rest();
//                return;
//            }
            DefaultHttpResponse httpResponse = RESPONSE_THREAD_LOCAL.get();
            httpResponse.init(session.getOutputStream());
            try {
                processHandle.doHandle(request, httpResponse);
            } catch (HttpException e) {
                e.printStackTrace();
                httpResponse.setHttpStatus(HttpStatus.valueOf(e.getHttpCode()));
                httpResponse.getOutputStream().write(e.getDesc().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                httpResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
                httpResponse.getOutputStream().write(e.fillInStackTrace().toString().getBytes());
            }
//
            httpResponse.getOutputStream().close();


            if (!StringUtils.equalsIgnoreCase(HttpHeaderConstant.Values.KEEPALIVE, request.getHeader(HttpHeaderConstant.Names.CONNECTION)) || httpResponse.getHttpStatus() != HttpStatus.OK) {
                LOGGER.info("will close session");
                session.close(false);
            }
//            session.getOutputStream().write(b);

        } catch (IOException e) {
            LOGGER.error("IO Exception", e);
        }
        request.rest();
    }

    @Override
    public void stateEvent(AioSession<Http11Request> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
//        LOGGER.info(stateMachineEnum+" "+session.getSessionID());
        if (throwable != null) {
//            LOGGER.error("",throwable);
//            System.exit(1);
        }
        switch (stateMachineEnum) {
            case NEW_SESSION:
//                LOGGER.info("new connection:{}", session);
                session.setAttachment(new Http11Request());
                break;
//            case FLOW_LIMIT:
//                LOGGER.warn("流控");
//                break;
//            case RELEASE_FLOW_LIMIT:
//                LOGGER.warn("释放流控");
//                break;
            case PROCESS_EXCEPTION:
                LOGGER.error("process request exception", throwable);
                session.close();
                break;
            case OUTPUT_EXCEPTION:
                LOGGER.error("", throwable);
                break;
            case SESSION_CLOSED:
//                LOGGER.info("connection closed:{}", session);
                break;
        }
    }

    public void route(String urlPattern, HttpHandle httpHandle) {
        routeHandle.route(urlPattern, httpHandle);
    }
}
