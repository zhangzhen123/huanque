package com.julun.huanque.common.bean.message;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * @创建者 dong
 * @创建时间 2020/6/16 11:02
 * @描述 自定义消息  * value 即 ObjectName 是消息的唯一标识不可以重复，开发者命名时不能以 RC 开头，避免和融云内置消息冲突； flag 是用来定义消息的可操作状态。
 */
@MessageTag(value = "s:privateChatCustomSimulateMessage", flag = MessageTag.ISPERSISTED)
public class CustomSimulateMessage extends MessageContent {

    public static final Creator<CustomSimulateMessage> CREATOR = new Creator<CustomSimulateMessage>() {
        @Override
        public CustomSimulateMessage createFromParcel(Parcel in) {
            return new CustomSimulateMessage(in);
        }

        @Override
        public CustomSimulateMessage[] newArray(int size) {
            return new CustomSimulateMessage[size];
        }
    };

    private String context;

    private String type;

    private String extra;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    // 快速构建消息对象方法
    public static CustomSimulateMessage obtain() {
        CustomSimulateMessage model = new CustomSimulateMessage();
        return model;
    }

    public CustomSimulateMessage(Parcel in) {
        context = in.readString();
        type = in.readString();
        extra = in.readString();
    }

    // 默认构造方法
    private CustomSimulateMessage() {

    }

    /**
     * 创建 CustomMessage(byte[] data) 带有 byte[] 的构造方法用于解析消息内容.
     */
    public CustomSimulateMessage(byte[] data) {
        if (data == null) {
            return;
        }

        String jsonStr = null;

        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (jsonStr == null || jsonStr.isEmpty()) {
            return;
        }
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);

            type = jsonObj.optString("type");

            context = jsonObj.optString("context");

            extra = jsonObj.optString("extra");

            // 消息携带用户信息时, 自定义消息需添加下面代码
            if (jsonObj.has("user")) {
                setUserInfo(parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }

            // 用于群组聊天, 消息携带 @ 人信息时, 自定义消息需添加下面代码
            if (jsonObj.has("mentionedInfo")) {
                setMentionedInfo(parseJsonToMentionInfo(jsonObj.getJSONObject("mentionedInfo")));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {

            // 消息携带用户信息时, 自定义消息需添加下面代码
            if (getJSONUserInfo() != null) {
                jsonObj.putOpt("user", getJSONUserInfo());
            }

            // 用于群组聊天, 消息携带 @ 人信息时, 自定义消息需添加下面代码
            if (getJsonMentionInfo() != null) {
                jsonObj.putOpt("mentionedInfo", getJsonMentionInfo());
            }

            // ...
            // 自定义消息, 定义的字段.
            if (context != null) {
                jsonObj.putOpt("context", context);
            }
            if (type != null) {
                jsonObj.put("type", type);
            }

            if(extra != null){
                jsonObj.put("extra",extra);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(context);
        dest.writeString(type);
        dest.writeString(extra);
    }
}
