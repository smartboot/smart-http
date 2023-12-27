package org.smartboot.http.client;

public interface WebSocketListener {

    default void onOpen(WebSocketClient client, WebSocketResponse session) {
        System.out.println("连接已打开");
    }


    default void onClose(WebSocketClient client, int code, String reason) {
        System.out.println("连接已关闭");
    }

    //
    default void onError(WebSocketClient client, WebSocketResponse session, Throwable throwable) {
        System.out.println("发生错误： " + throwable.getMessage());
    }

    default void onMessage(WebSocketClient client, String message) {
        System.out.println("收到消息： " + message);
    }

    default void onMessage(WebSocketClient client, byte[] message) {
        System.out.println("收到消息： " + message);
    }
}
