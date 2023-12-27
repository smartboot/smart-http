package org.smartboot.http.client;

import org.smartboot.http.client.impl.HttpMessageProcessor;
import org.smartboot.http.client.impl.HttpResponseProtocol;
import org.smartboot.http.client.impl.ResponseAttachment;
import org.smartboot.http.client.impl.WebSocketRequestImpl;
import org.smartboot.http.client.impl.WebSocketResponseImpl;
import org.smartboot.http.common.codec.websocket.BasicFrameDecoder;
import org.smartboot.http.common.codec.websocket.Decoder;
import org.smartboot.http.common.codec.websocket.WebSocket;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.Constant;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.WebSocketUtil;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.buffer.VirtualBuffer;
import org.smartboot.socket.extension.plugins.Plugin;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.factory.ClientSSLContextFactory;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.util.AttachKey;
import org.smartboot.socket.util.Attachment;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class WebSocketClient {


    private final HttpClientConfiguration configuration;

    /**
     * Header: Host
     */
    private final String hostHeader;
    /**
     * 客户端Client
     */
    private AioQuickClient client;

    /**
     * 绑定线程池资源组
     */
    private AsynchronousChannelGroup asynchronousChannelGroup;

    private boolean connected;

    private boolean firstConnected = true;

    /**
     * Http 解码协议
     */
    private final HttpResponseProtocol protocol = HttpResponseProtocol.INSTANCE;
    /**
     * 消息处理器
     */
    private final HttpMessageProcessor processor = new HttpMessageProcessor();

    private final String uri;
    private WebSocketRequestImpl request;
    protected final CompletableFuture<WebSocketResponseImpl> completableFuture = new CompletableFuture<>();
    private WebSocketListener listener;
    private final Decoder basicFrameDecoder = new BasicFrameDecoder();
    private static final AttachKey<Decoder> FRAME_DECODER_KEY = AttachKey.valueOf("ws_frame_decoder");
    private WebSocketResponseImpl webSocketResponse;

    public static void main(String[] args) throws IOException {
        WebSocketClient client = new WebSocketClient("ws://localhost:8080");
        client.configuration().debug(true);
        client.connect(new WebSocketListener() {
            @Override
            public void onOpen(WebSocketClient client, WebSocketResponse session) {
                try {
                    client.sendMessage("hello");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onMessage(WebSocketClient client, String message) {
                System.out.println(message);
            }
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    client.sendMessage("aaa" + System.currentTimeMillis());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public WebSocketClient(String url) {
        int schemaIndex = url.indexOf("://");
        if (schemaIndex == -1) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        String schema = url.substring(0, schemaIndex);
        int uriIndex = url.indexOf("/", schemaIndex + 3);
        int portIndex = url.indexOf(":", schemaIndex + 3);
        boolean http = Constant.SCHEMA_WS.equals(schema);
        boolean https = !http && Constant.SCHEMA_WSS.equals(schema);

        if (!http && !https) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        String host;
        int port;
        if (portIndex > 0) {
            host = url.substring(schemaIndex + 3, portIndex);
            port = NumberUtils.toInt(uriIndex > 0 ? url.substring(portIndex + 1, uriIndex) : url.substring(portIndex + 1), -1);
        } else if (uriIndex > 0) {
            host = url.substring(schemaIndex + 3, uriIndex);
            port = https ? 443 : 80;
        } else {
            host = url.substring(schemaIndex + 3);
            port = https ? 443 : 80;
        }
        if (port == -1) {
            throw new IllegalArgumentException("invalid url:" + url);
        }
        this.configuration = new HttpClientConfiguration(host, port);
        configuration.setHttps(https);
        hostHeader = configuration.getHost() + ":" + configuration.getPort();
        this.uri = uriIndex > 0 ? url.substring(uriIndex) : "/";

    }

    public HttpClientConfiguration configuration() {
        return configuration;
    }

    public void connect(WebSocketListener listener) throws IOException {
        this.listener = listener;
        if (connected) {
            AioSession session = client.getSession();
            if (session == null || session.isInvalid()) {
                close();
                connect(listener);
            }
            return;
        }

        try {
            if (firstConnected) {
                boolean noneSslPlugin = true;
                for (Plugin<AbstractResponse> responsePlugin : configuration.getPlugins()) {
                    processor.addPlugin(responsePlugin);
                    if (responsePlugin instanceof SslPlugin) {
                        noneSslPlugin = false;
                    }
                }
                if (noneSslPlugin && configuration.isHttps()) {
                    processor.addPlugin(new SslPlugin<>(new ClientSSLContextFactory()));
                }

                firstConnected = false;
            }
            connected = true;
            client = configuration.getProxy() == null ? new AioQuickClient(configuration.getHost(), configuration.getPort(), protocol, processor) : new AioQuickClient(configuration.getProxy().getProxyHost(), configuration.getProxy().getProxyPort(), protocol, processor);
            BufferPagePool readPool = configuration.getReadBufferPool();
            client.setBufferPagePool(configuration.getWriteBufferPool()).setReadBufferFactory(bufferPage -> readPool == null ? VirtualBuffer.wrap(ByteBuffer.allocate(configuration.readBufferSize())) : readPool.allocateBufferPage().allocate(configuration.readBufferSize()));
            if (configuration.getConnectTimeout() > 0) {
                client.connectTimeout(configuration.getConnectTimeout());
            }
            if (asynchronousChannelGroup == null) {
                client.start();
            } else {
                client.start(asynchronousChannelGroup);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        AioSession session = client.getSession();
        webSocketResponse = new WebSocketResponseImpl(session);
        ResponseAttachment attachment = session.getAttachment();
        attachment.setWs(true);
        attachment.setResponse(webSocketResponse);
        initRest();
    }

    private void initRest() throws IOException {
        request = new WebSocketRequestImpl(client.getSession());
        request.setUri(uri);
        request.setProtocol(HttpProtocolEnum.HTTP_11.getProtocol());
        request.addHeader(HeaderNameEnum.HOST.getName(), hostHeader);
        request.addHeader(HeaderNameEnum.UPGRADE.getName(), HeaderValueEnum.WEBSOCKET.getName());
        request.setHeader(HeaderNameEnum.CONNECTION.getName(), HeaderValueEnum.UPGRADE.getName());
        request.setHeader(HeaderNameEnum.Sec_WebSocket_Key.getName(), generateSecWebSocketKey());
        request.setHeader(HeaderNameEnum.Sec_WebSocket_Version.getName(), "13");
//        request.setHeader(HeaderNameEnum.Sec_WebSocket_Protocol.getName(), HeaderValueEnum.PERMESSAGE_DEFLATE.getName());
        completableFuture.thenAccept(new Consumer<WebSocketResponseImpl>() {
            @Override
            public void accept(WebSocketResponseImpl webSocketResponse) {
                try {
                    switch (webSocketResponse.getFrameOpcode()) {
                        case WebSocketUtil.OPCODE_TEXT:
                            listener.onMessage(WebSocketClient.this, new String(webSocketResponse.getPayload(), StandardCharsets.UTF_8));
                            break;
                        case WebSocketUtil.OPCODE_BINARY:
                            listener.onMessage(WebSocketClient.this, webSocketResponse.getPayload());
                            break;
                        case WebSocketUtil.OPCODE_PING:
//                                handlePing(request, response);
                            break;
                        case WebSocketUtil.OPCODE_PONG:
//                                handlePong(request, response);
                            break;
                        case WebSocketUtil.OPCODE_CONTINUE:
//                                LOGGER.warn("unSupport OPCODE_CONTINUE now,ignore payload: {}", StringUtils.toHexString(request.getPayload()));
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }
                } catch (Throwable throwable) {
                    listener.onError(WebSocketClient.this, webSocketResponse, throwable);
                    throw throwable;
                }
            }
        });
        processor.getQueue(client.getSession()).offer(new QueueUnit(completableFuture, new ResponseHandler() {
            @Override
            public void onHeaderComplete(AbstractResponse abstractResponse) throws IOException {
                WebSocketResponseImpl webSocketResponse = (WebSocketResponseImpl) abstractResponse;
                super.onHeaderComplete(webSocketResponse);
                System.out.println(webSocketResponse.getStatus());
                if (webSocketResponse.getStatus() != HttpStatus.SWITCHING_PROTOCOLS.value()) {
                    listener.onClose(WebSocketClient.this, abstractResponse.getStatus(), abstractResponse.getReasonPhrase());
                    return;
                }

                webSocketResponse.getAttachment().put(FRAME_DECODER_KEY, basicFrameDecoder);
                listener.onOpen(WebSocketClient.this, webSocketResponse);
            }

            @Override
            public boolean onBodyStream(ByteBuffer buffer, AbstractResponse abstractResponse) {
                WebSocketResponseImpl webSocketResponse = (WebSocketResponseImpl) abstractResponse;
                Attachment attachment = webSocketResponse.getAttachment();
                Decoder decoder = attachment.get(FRAME_DECODER_KEY).decode(buffer, webSocketResponse);
                if (decoder == WebSocket.PAYLOAD_FINISH) {
                    attachment.put(FRAME_DECODER_KEY, basicFrameDecoder);
                    return true;
                } else {
                    attachment.put(FRAME_DECODER_KEY, decoder);
                    return false;
                }

            }

        }));
        request.getOutputStream().flush();
    }

    /**
     * 在客户端握手中的|Sec-WebSocket-Key|头字段包括一个 base64 编码的值，如果解码，长度是 16 字节。
     */
    private String generateSecWebSocketKey() {
        byte[] keyBytes = new byte[16];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(keyBytes);
    }


    public void sendMessage(String message) throws IOException {
        // 发送消息到服务器
        WebSocketUtil.send(request.getOutputStream(), WebSocketUtil.OPCODE_TEXT, message.getBytes(), 0, message.length());
        request.getOutputStream().flush();
    }

    public void setAsynchronousChannelGroup(AsynchronousChannelGroup asynchronousChannelGroup) {
        this.asynchronousChannelGroup = asynchronousChannelGroup;
    }

    public void close() {
        connected = false;
        client.shutdownNow();
    }

}
