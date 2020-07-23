package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.julun.huanque.common.utils.ULog;
import com.julun.rnlib.RNPageActivity;
import com.julun.rnlib.RnManager;

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
    public void openPageByName(String pageName, ReadableMap params, Promise promise) {
        ULog.Companion.i("openPageByName :" + pageName);
        try {
            RNPageActivity activity = RnManager.INSTANCE.getCurActivity();
            if (activity != null) {
                activity.openPager(pageName, params);
                RnManager.INSTANCE.getPromiseMap().put(pageName, promise);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
