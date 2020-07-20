package com.julun.rnlib.module;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.julun.huanque.common.constant.ARouterConstant;
import com.julun.huanque.common.constant.OperationType;
import com.julun.huanque.common.constant.ParamConstant;
import com.julun.rnlib.RnConstant;

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
                    Boolean follow = params.getBoolean("follow");
                    //todo

                    break;
                }
                case  RnConstant.MY_PROFILE_CHANGE: {
                    int id = params.getInt("userId");
                    String nickname = params.getString("nickname");
                    //todo
                    break;
                }
                default:{

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
