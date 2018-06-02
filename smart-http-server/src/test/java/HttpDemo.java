import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.HttpPerformanceProcessor;

/**
 * @author 三刀
 * @version V1.0 , 2018/3/28
 */
public class HttpDemo {
    public static void main(String[] args) {
        HttpPerformanceProcessor processor = new HttpPerformanceProcessor();
        HttpBootstrap.http(processor);
    }
}
