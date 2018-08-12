package org.smartboot.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.HttpEntityV2;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.server.handle.RouteHandle;
import org.smartboot.http.server.handle.http11.RFC2612RequestHandle;
import org.smartboot.http.server.handle.http11.ResponseHandle;
import org.smartboot.http.server.http11.DefaultHttpResponse;
import org.smartboot.http.server.http11.HttpResponse;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpV2MessageProcessor implements MessageProcessor<HttpEntityV2> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpV2MessageProcessor.class);
    private static String b = "HTTP/1.1 200 OK\r\n" +
            "Server:smart-socket\r\n" +
            "Connection:keep-alive\r\n" +
            "Host:localhost\r\n" +
            "Content-Length:31\r\n" +
            "Date:Wed, 11 Apr 2018 12:35:01 GMT\r\n\r\n" +
            "Hello smart-socket http server!";

    /**
     * Http消息处理器
     */
    private HttpHandle processHandle;

    private RouteHandle routeHandle;

    private ResponseHandle responseHandle;

    public HttpV2MessageProcessor(String baseDir) {
        processHandle = new RFC2612RequestHandle();
        routeHandle = new RouteHandle(baseDir);
        processHandle.next(routeHandle);

        responseHandle = new ResponseHandle();
    }

    @Override
    public void process(AioSession<HttpEntityV2> session, HttpEntityV2 entry) {

        try {
//            session.write(ByteBuffer.wrap(b.getBytes()));
            processHttp11(session, entry);
//            session.write(ByteBuffer.wrap(b.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        entry.rest();
    }

    private void processHttp11(final AioSession<HttpEntityV2> session, HttpEntityV2 request) throws IOException {
        HttpResponse httpResponse = new DefaultHttpResponse(session, request, responseHandle);
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

//        if (!StringUtils.equalsIgnoreCase(HttpHeaderConstant.Values.KEEPALIVE, request.getHeader(HttpHeaderConstant.Names.CONNECTION)) || httpResponse.getHttpStatus() != HttpStatus.OK) {
//            session.close(false);
//        }
    }

    @Override
    public void stateEvent(AioSession<HttpEntityV2> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
//            System.exit(0);
//            return;
        }
        switch (stateMachineEnum) {
            case NEW_SESSION:
                session.setAttachment(new HttpEntityV2());
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
