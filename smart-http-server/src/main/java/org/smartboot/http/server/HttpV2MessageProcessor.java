package org.smartboot.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.http.common.HttpEntity;
import org.smartboot.http.common.HttpEntityV2;
import org.smartboot.socket.Filter;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.extension.timer.QuickMonitorTimer;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;

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

    public static void main(String[] args) {
        AioQuickServer<HttpEntityV2> server = new AioQuickServer<HttpEntityV2>(8888, new HttpServerV2Protocol(), new HttpV2MessageProcessor());
        server.setWriteQueueSize(0)
                .setReadBufferSize(128)
//        .setDirectBuffer(true)
        ;

        server.setFilters(new Filter[]{new QuickMonitorTimer<HttpEntity>()});
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(AioSession<HttpEntityV2> session, HttpEntityV2 msg) {
        try {
//            LOGGER.info(msg.toString());
//            msg.rest();
            session.write(ByteBuffer.wrap(b.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            msg.rest();
        }
    }

    @Override
    public void stateEvent(AioSession<HttpEntityV2> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
//                Attachment attachment = new Attachment();
                session.setAttachment(new HttpEntityV2());
//                attachment.put(HttpServerV2Protocol.HTTP_ENTITY_V_2_ATTACH_KEY, new HttpEntityV2());
//                attachment.put(HttpServerV2Protocol.STATE_ATTACH_KEY, HttpServerV2Protocol.State.verb);
                break;
            case PROCESS_EXCEPTION:
                session.close();
                break;
        }
    }
}
