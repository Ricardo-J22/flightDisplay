package com.debei.utils;

/**
 * Created by yzq on 2017/9/27.
 * 传递消息时使用，可以自己增加更多的参数
 */

public class EventMsg {

    private String Tag;
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }
}
