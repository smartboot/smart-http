/*******************************************************************************
 * Copyright (c) 2017-2022, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: TimerUtils.java
 * Date: 2022-02-04
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.common.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/2/4
 */
public class TimerUtils {

    /**
     * 当前时间
     */
    private static long currentTimeMillis;

    static {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "timer");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                currentTimeMillis = System.currentTimeMillis();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }
}
