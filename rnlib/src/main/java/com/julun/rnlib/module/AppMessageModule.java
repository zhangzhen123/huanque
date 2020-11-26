package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.julun.huanque.common.basic.NetStateType;
import com.julun.huanque.common.basic.QueryType;
import com.julun.huanque.common.basic.ReactiveData;
import com.julun.huanque.common.bean.beans.UserInfoChangeResult;
import com.julun.huanque.common.bean.events.UserInfoEditEvent;
import com.julun.huanque.common.constant.BusiConstant;
import com.julun.huanque.common.manager.HuanViewModelManager;
import com.julun.huanque.common.utils.GlobalUtils;
import com.julun.huanque.common.utils.SessionUtils;
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
                    String follow = null;
                    if (params.hasKey("follow")) {
                        follow = params.getString("follow");
                    }
                    if (follow == null) {
                        follow = "";
                    }
                    if (stranger == null) {
                        stranger = "";
                    }
                    GlobalUtils.INSTANCE.updateStrangerData(userId, stranger.equals(BusiConstant.True));
//                    EventBus.getDefault().post(new UserInfoChangeEvent((long) userId, stranger.equals(BusiConstant.True), follow));
                    UserInfoChangeResult changeResult = new UserInfoChangeResult(userId, follow, "", stranger.equals(BusiConstant.True));
                    HuanViewModelManager.INSTANCE.getHuanQueViewModel().getUserInfoStatusChange().setValue(
                            new ReactiveData<UserInfoChangeResult>(NetStateType.SUCCESS, changeResult, QueryType.INIT, null));

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
                        SessionUtils.INSTANCE.setNickName(nickname);
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
                            ReadableMap obj = picList.getMap(i);
                            if (obj != null && obj.hasKey("coverPic")) {
                                list.add(obj.getString("coverPic"));
                            }
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
