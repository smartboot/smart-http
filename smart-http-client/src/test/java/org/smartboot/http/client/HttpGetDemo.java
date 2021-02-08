/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: Benchmark.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.client;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/2/7
 */
public class HttpGetDemo {
    public static void main(String[] args) {
        HttpClient httpClient = new HttpClient("www.baidu.com", 80);
        httpClient.connect();
        httpClient.get("/")
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                })
                .onFailure(throwable -> throwable.printStackTrace())
                .send();

    }

}
