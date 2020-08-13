package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.julun.huanque.common.bean.events.UserInfoChangeEvent;
import com.julun.huanque.common.bean.events.UserInfoEditEvent;
import com.julun.huanque.common.constant.BusiConstant;
import com.julun.huanque.common.utils.GlobalUtils;
import com.julun.huanque.common.utils.ULog;
import com.julun.rnlib.RnConstant;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class AppMessageModule extends ReactContextBaseJavaModule {

    public AppMessageModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean canOverrideExistingModule() {
        return true;
    }

    @ReactMethod
    public void sendAction(String type, ReadableMap params, Promise promise) {
        try {
            switch (type) {
                case RnConstant.FOLLOW_USER_CHANGE: {
                    // Boolean follow = params.getBoolean("follow");
                    int userId = 0;
                    if (params.hasKey("userId")) {
                        userId = params.getInt("userId");
                    }
                    String stranger = null;
                    if (params.hasKey("stranger")) {
                        stranger = params.getString("stranger");
                    }
                    if (stranger != null) {
                        GlobalUtils.INSTANCE.updateStrangerData(userId, stranger.equals(BusiConstant.True));
                        new UserInfoChangeEvent();
                        EventBus.getDefault().post(new UserInfoChangeEvent((long) userId, stranger.equals(BusiConstant.True)));
                    }

                    break;
                }
                case RnConstant.MY_PROFILE_CHANGE: {
                    ULog.Companion.i("params=" + params);
                    int userId = 0;
                    if (params.hasKey("userId")) {
                        userId = params.getInt("userId");
                    }
                    boolean stranger = false;
                    if (params.hasKey("stranger")) {
                        stranger = params.getBoolean("stranger");
                    }
                    String nickname = "";
                    if (params.hasKey("nickname")) {
                        nickname = params.getString("nickname");
                    }
                    String headPic = "";
                    if (params.hasKey("headPic")) {
                        headPic = params.getString("headPic");
                    }
                    ReadableArray picList = null;
                    if (params.hasKey("picList")) {
                        picList = params.getArray("picList");
                    }

                    ArrayList<String> list = new ArrayList<>();
                    if (picList != null) {
                        for (int i = 0; i < picList.size(); i++) {
                            list.add(picList.getString(i));
                        }

                    }
                    EventBus.getDefault().post(new UserInfoEditEvent(userId, stranger, nickname, headPic, list));
                    break;
                }
                default: {

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
