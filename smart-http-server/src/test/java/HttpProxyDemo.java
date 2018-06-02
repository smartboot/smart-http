import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.AbstractHttpEntity;
import org.smartboot.http.server.http11.Http11Request;
import org.smartboot.socket.transport.AioSession;

/**
 * @author 三刀
 * @version V1.0 , 2018/6/2
 */
public class HttpProxyDemo {
    public static void main(String[] args) {
        HttpBootstrap.http(new MessageProcessor<AbstractHttpEntity>() {
            @Override
            public void process(AioSession<AbstractHttpEntity> session, AbstractHttpEntity msg) {
                Http11Request request = (Http11Request) msg;
                System.out.println(request.getHeader("Host"));
                System.out.println(request.getRequestURI());
//                AioQuickClient client=new AioQuickClient();
            }

            @Override
            public void stateEvent(AioSession<AbstractHttpEntity> session, StateMachineEnum stateMachineEnum, Throwable throwable) {

            }
        });
    }
}
