# smart-http
smart-http 是一款比较简易的 http服务器，其通信内核采用了[**smart-socket**](https://gitee.com/smartboot/smart-socket)最新版`v1.4.6`。

也正因使用了 smart-socket，该服务器的性能表现还是非常不错的，在本人的4核CPU下能跑出73W+的 qps。

smart-socket 的每次性能测试都是基于该服务器进行的，相信 smart-http 的表现不会让您失望的。


感兴趣的朋友欢迎加入QQ群交流：830015805，入群的前提是 Star 一下我们的项目，所以请不要吝啬您的星 ^_^
## 功能列表
1. 支持GET、POST的HTTP请求。
2. 提供了URL路由组件，可以快速搭建一套静态服务器。
3. 支持部分RFC2612规范，后续会逐渐完善。
4. 支持Https协议，由smart-socket为其赋能。
5. 具备文件上传的能力。

## 快速体验
1. 在您的Maven工程中引入smart-http依赖。
    ```xml
    <dependency>
        <groupId>org.smartboot.http</groupId>
        <artifactId>smart-http-server</artifactId>
        <version>1.0.12</version>
    </dependency>
    ```
2. 拷贝以下代码并启动。
    ```java
    public class SimpleSmartHttp {
        public static void main(String[] args) {
            HttpBootstrap bootstrap = new HttpBootstrap();
            bootstrap.pipeline().next(new HttpHandle() {
                @Override
                public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                    response.write("hello world".getBytes());
                }
            });
            bootstrap.setPort(8080).start();
        }
    }
    ```
3. 浏览器访问:`http://localhost:8080/`，

