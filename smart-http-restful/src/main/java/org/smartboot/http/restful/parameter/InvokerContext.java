package org.smartboot.http.restful.parameter;

import com.alibaba.fastjson2.JSONObject;

public class InvokerContext {
    private JSONObject jsonObject;

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

}
