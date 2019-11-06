package org.smartboot.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.Pipeline;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.handle.HandlePipeline;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Http11Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMessageProcessor.class);
    private ThreadLocal<DefaultHttpResponse> RESPONSE_THREAD_LOCAL = null;
    private HandlePipeline pipeline = new HandlePipeline();

    public HttpMessageProcessor() {
        pipeline.next(new RFC2612RequestHandle());

        RESPONSE_THREAD_LOCAL = new ThreadLocal<DefaultHttpResponse>() {
            @Override
            protected DefaultHttpResponse initialValue() {
                return new DefaultHttpResponse();
            }
        };
    }

    @Override
    public void process(AioSession<Http11Request> session, Http11Request request) {
        try {
            DefaultHttpResponse httpResponse = RESPONSE_THREAD_LOCAL.get();
            httpResponse.init(session.writeBuffer());
//            boolean isKeepAlive = StringUtils.equalsIgnoreCase(HttpHeaderConstant.Values.KEEPALIVE, request.getHeader(HttpHeaderConstant.Names.CONNECTION));
            try {
                //用ab进行测试时需要带上该响应
//                if (isKeepAlive) {
//                    httpResponse.setHeader(HttpHeaderConstant.Names.CONNECTION, HttpHeaderConstant.Values.KEEPALIVE);
//                }
                pipeline.doHandle(request, httpResponse);
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
            if (!httpResponse.isClosed()) {
                httpResponse.getOutputStream().close();
            }


//
//            if (!isKeepAlive || httpResponse.getHttpStatus() != HttpStatus.OK) {
//                LOGGER.info("will close session");
//                session.close(false);
//            }
        } catch (IOException e) {
            LOGGER.error("IO Exception", e);
        }
        request.rest();
    }

    @Override
    public void stateEvent(AioSession<Http11Request> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                session.setAttachment(new Http11Request(session));
                break;
            case PROCESS_EXCEPTION:
                LOGGER.error("process request exception", throwable);
                session.close();
                break;
//            case INPUT_SHUTDOWN:
//                LOGGER.error("inputShutdown", throwable);
//                break;
//            case OUTPUT_EXCEPTION:
//                LOGGER.error("", throwable);
//                break;
//            case INPUT_EXCEPTION:
//                LOGGER.error("",throwable);
//                break;
//            case SESSION_CLOSED:
//                System.out.println("closeSession");
//                LOGGER.info("connection closed:{}");
//                break;
            case DECODE_EXCEPTION:
                throwable.printStackTrace();
                break;
//                default:
//                    System.out.println(stateMachineEnum);
        }
    }

    public Pipeline pipeline(HttpHandle httpHandle) {
        return pipeline.next(httpHandle);
    }

    public Pipeline pipeline() {
        return pipeline;
    }
}
