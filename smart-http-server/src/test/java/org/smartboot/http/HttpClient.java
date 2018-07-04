package org.smartboot.http;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

/**
 * @author 三刀
 * @version V1.0 , 2018/7/4
 */
public class HttpClient {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        final ClientMessageProcessor processor = new ClientMessageProcessor();
        AioQuickClient<byte[]> aioQuickClient = new AioQuickClient<byte[]>("localhost", 8888, new Protocol<byte[]>() {
            @Override
            public byte[] decode(ByteBuffer byteBuffer, AioSession<byte[]> aioSession, boolean b) {
                byte[] array = new byte[byteBuffer.remaining()];
                byteBuffer.get(array);
                return array;
            }

            @Override
            public ByteBuffer encode(byte[] bytes, AioSession<byte[]> aioSession) {
                return ByteBuffer.wrap(bytes);
            }
        }, processor);
        aioQuickClient.start();
        byte[] a="GET / HTTP/1.1\r\nHost:".getBytes();
        final byte[] b=" a\r\n\r\n".getBytes();
        processor.session.write(a);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processor.session.write(b);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


}

class ClientMessageProcessor implements MessageProcessor<byte[]> {
    AioSession<byte[]> session;

    @Override
    public void process(AioSession<byte[]> aioSession, byte[] bytes) {

    }

    @Override
    public void stateEvent(AioSession<byte[]> aioSession, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                this.session = aioSession;
                break;
        }
    }
}