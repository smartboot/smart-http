# smart-http
smart-http 是一款比较简易的 http服务器，其通信内核采用了[**smart-socket**](https://gitee.com/smartboot/smart-socket)最新版`v1.4.9`。

也正因使用了 smart-socket，该服务器的性能表现还是非常不错的，在本人的4核CPU下能跑出73W+的 qps。

smart-socket 的每次性能测试都是基于该服务器进行的，相信 smart-http 的表现不会让您失望的。


感兴趣的朋友请记得 Star一下该项目。[下载体验包](https://gitee.com/smartboot/smart-http/attach_files)
## 功能列表
1. 支持GET、POST的HTTP请求。
2. 提供了URL路由组件，可以快速搭建一套静态服务器。
3. 支持部分RFC2612规范，后续会逐渐完善。
4. 支持Https协议，由smart-socket为其赋能。
5. 具备文件上传的能力。
6. 支持 websocket

## 快速体验
1. 在您的Maven工程中引入smart-http依赖。
    ```xml
    <dependency>
        <groupId>org.smartboot.http</groupId>
        <artifactId>smart-http-server</artifactId>
        <version>1.0.13</version>
    </dependency>
    ```
2. 拷贝以下代码并启动。
    ```java
    public class SimpleSmartHttp {
        public static void main(String[] args) {
            HttpBootstrap bootstrap = new HttpBootstrap();
            // 普通http请求
            bootstrap.pipeline().next(new HttpHandle() {
                @Override
                public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                    response.write("hello world<br/>".getBytes());
                }
            });
            // websocket请求
            bootstrap.wsPipeline().next(new WebSocketDefaultHandle() {
                @Override
                public void handleTextMessage(WebSocketRequest request, WebSocketResponse response, String data) {
                    response.sendTextMessage("Hello World");
                }
            });
            bootstrap.setPort(8080).start();
        }
    }
    ```
3. 浏览器访问:`http://localhost:8080/`，亦或采用websocket请求`ws://127.0.0.1:8080/`

