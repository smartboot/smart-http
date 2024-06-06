package org.smartboot.http.restful.sse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class SseEmitter {
    private final OutputStream outputStream;

    public SseEmitter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void send(String data) throws IOException {
        outputStream.write(("data:" + data + "\n\n").getBytes());
        outputStream.flush();
    }

    public synchronized void onTimeout(Runnable callback) {
//        this.timeoutCallback.setDelegate(callback);
    }

    public synchronized void onError(Consumer<Throwable> callback) {
//        this.errorCallback.setDelegate(callback);
    }

    public synchronized void onCompletion(Runnable callback) {
//        this.completionCallback.setDelegate(callback);
    }

    public void complete() {

    }
}
