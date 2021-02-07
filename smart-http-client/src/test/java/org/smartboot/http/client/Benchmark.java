/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Benchmark.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

import org.smartboot.socket.buffer.BufferPagePool;

import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/7
 */
public class Benchmark {
    public static void main(String[] args) throws InterruptedException, IOException {
        long start = System.currentTimeMillis();

        int time = 15 * 1000;
//        int time = Integer.MAX_VALUE;
        int threadNum = 4;
        int connectCount = 1024;
        int pipeline = 16;
        AtomicLong success = new AtomicLong(0);
        AtomicLong fail = new AtomicLong(0);
        AtomicBoolean running = new AtomicBoolean(true);

        AsynchronousChannelGroup asynchronousChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(threadNum, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r);
            }
        });
        BufferPagePool bufferPagePool = new BufferPagePool(10 * 1023 * 1024, threadNum, true);
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        for (int i = 0; i < connectCount; i++) {
            HttpClient httpClient = new HttpClient("127.0.0.1", 8080);
            httpClient.setAsynchronousChannelGroup(asynchronousChannelGroup);
            httpClient.setWriteBufferPool(bufferPagePool);
            httpClient.setExecutorService(executorService);
            Consumer<Throwable> failure = new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    fail.incrementAndGet();
                    throwable.printStackTrace();
                }
            };
            Consumer<HttpResponse> consumer = new Consumer<HttpResponse>() {
                @Override
                public void accept(HttpResponse response) {
                    success.incrementAndGet();
//                    System.out.println(response.body());
                    if (running.get()) {
                        httpClient.get("/plaintext").send().onSuccess(this).onFailure(failure);
                    } else {
                        httpClient.close();
                    }
                }
            };

            httpClient.get("/plaintext")
                    .onSuccess(consumer)
                    .onFailure(failure)
                    .send();
        }

        Thread.sleep(time);
        running.set(false);

        System.out.println("cost:" + (System.currentTimeMillis() - start));

        System.out.println("success:" + success.get());
        System.out.println("fail:" + fail.get());
    }

}
