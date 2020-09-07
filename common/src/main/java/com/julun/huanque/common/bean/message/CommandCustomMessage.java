package com.julun.huanque.common.bean.message;

import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * @创建者 dong
 * @创建时间 2020/9/5 15:51
 * @描述 语音会话相关自定义消息
 */
@MessageTag(value = "HQ:NetCall", flag = MessageTag.ISCOUNTED)
public class CommandCustomMessage extends MessageContent {

    public static final Creator<CommandCustomMessage> CREATOR = new Creator<CommandCustomMessage>() {
        @Override
        public CommandCustomMessage createFromParcel(Parcel in) {
            return new CommandCustomMessage(in);
        }

        @Override
        public CommandCustomMessage[] newArray(int size) {
            return new CommandCustomMessage[size];
        }
    };

    // 消息内容
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // 快速构建消息对象方法
    public static CommandCustomMessage obtain() {
        CommandCustomMessage model = new CommandCustomMessage();
        return model;
    }

    public CommandCustomMessage(Parcel in) {
        data = in.readString();
    }

    // 默认构造方法
    public CommandCustomMessage() {

    }

    /**
     * 创建 CustomMessage(byte[] data) 带有 byte[] 的构造方法用于解析消息内容.
     */
    public CommandCustomMessage(byte[] data) {
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

            this.data = jsonObj.optString("data");

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
            if (data != null) {
                jsonObj.putOpt("data", data);
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
        dest.writeString(data);
    }
}
