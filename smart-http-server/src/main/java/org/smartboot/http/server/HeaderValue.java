package org.smartboot.http.server;

/**
 * 支持多Value的Header
 *
 * @author 三刀
 * @version V1.0 , 2019/11/30
 */
class HeaderValue {
    /**
     * name
     */
    private String name;
    /**
     * Value 值
     */
    private String value;
    /**
     * 同名Value
     */
    private HeaderValue nextValue;

    private HeaderValue lastValue;

    public HeaderValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public HeaderValue getLastValue() {
        return lastValue;
    }

    public void setLastValue(HeaderValue lastValue) {
        this.lastValue = lastValue;
    }

    public HeaderValue getNextValue() {
        return nextValue;
    }

    public void setNextValue(HeaderValue nextValue) {
        this.nextValue = nextValue;
    }
}
