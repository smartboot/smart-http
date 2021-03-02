package org.smartboot.http.client;

import org.smartboot.http.common.utils.HttpHeaderConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huqiang
 * @since 2021/3/2 10:57
 */
public class HttpPostDemo {

    public static void main(String[] args) {
        HttpClient httpClient = new HttpClient("localhost", 8080);
        httpClient.connect();
        Map<String, String> param = new HashMap<>();
        param.put("name", "zhouyu");
        param.put("age", "18");
        httpClient.post("/")
                .setContentType(HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                })
                .onFailure(Throwable::printStackTrace)
                .send(param);


        httpClient.post("/")
                .setContentType(HttpHeaderConstant.Values.X_WWW_FORM_URLENCODED)
                .onSuccess(response -> {
                    System.out.println(response.body());
                    httpClient.close();
                })
                .onFailure(Throwable::printStackTrace)
                .send();


    }

}
