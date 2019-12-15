package org.smartboot.http.server;

import org.smartboot.http.Pipeline;
import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.http.server.handle.HandlePipeline;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.utils.Attachment;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/10
 */
public class HttpMessageProcessor implements MessageProcessor<Http11Request> {
    private final HandlePipeline pipeline = new HandlePipeline();

    public HttpMessageProcessor() {
        pipeline.next(new RFC2612RequestHandle());
    }

    @Override
    public void process(AioSession<Http11Request> session, Http11Request request) {
        try {
            Http11Response httpResponse = request.getResponse();
            try {
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
            if (!httpResponse.isClosed()) {
                httpResponse.getOutputStream().close();
            }
            //Post请求没有读完Body，关闭通道
            if (request.getMethodEnum() == HttpMethodEnum.POST
                    && !StringUtils.startsWith(request.getContentType(), HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)
                    && request.getInputStream().available() > 0) {
                session.close(false);
            } else {
                request.rest();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void stateEvent(AioSession<Http11Request> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                Attachment attachment = new Attachment();
                attachment.put(HttpRequestProtocol.ATTACH_KEY_REQUEST, new Http11Request(session));
                session.setAttachment(attachment);
                break;
            case PROCESS_EXCEPTION:
                throwable.printStackTrace();
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
