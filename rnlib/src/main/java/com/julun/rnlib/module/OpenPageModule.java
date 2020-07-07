package com.julun.rnlib.module;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

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
    public void openPageByName(String pageName, Map<String,String> params) {
        try {
            switch (pageName) {
                case "page1": {
                    Activity currentActivity = getCurrentActivity();
                    if (currentActivity != null) {
                        Class activityClass = Class.forName(pageName);
                        Intent intent = new Intent(currentActivity, activityClass);
                        currentActivity.startActivity(intent);
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
