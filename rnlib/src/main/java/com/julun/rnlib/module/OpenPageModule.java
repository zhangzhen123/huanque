package com.julun.rnlib.module;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.julun.huanque.common.constant.ARouterConstant;
import com.julun.huanque.common.constant.BusiConstant;

import java.util.Map;

/**
 * 打开指定页面的交互模块
 */
public class OpenPageModule extends ReactContextBaseJavaModule {
    public OpenPageModule(ReactApplicationContext reactContext) {
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
    public void openPageByName(String pageName, Map<String, String> params) {
        try {
            switch (pageName) {
                case "PrivateMessagePage": {
                    String id = params.get("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle bundle = new Bundle();
                        bundle.putLong("TARGETID", userId);
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation(getCurrentActivity());
                    }

                    break;
                }
                case "TelephoneCallPage": {
                    ARouter.getInstance().build(ARouterConstant.VOICE_CHAT_ACTIVITY).navigation(getCurrentActivity());
                    break;
                }
                case "SendGiftPage": {
                    String id = params.get("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle bundle = new Bundle();
                        bundle.putLong("TARGETID", userId);
                        //todo 打开相关页面的参数
//                        bundle.putInt("open");
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation(getCurrentActivity());
                    }
                    break;
                }
                case "RecordVoicePage": {

                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
