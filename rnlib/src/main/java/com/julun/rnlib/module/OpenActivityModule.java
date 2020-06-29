package com.julun.rnlib.module;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class OpenActivityModule extends ReactContextBaseJavaModule {
    public OpenActivityModule(ReactApplicationContext reactContext) {
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
    public void startActivityByClassName(String className) {
        try {
            Activity currentActivity = getCurrentActivity();
            if (currentActivity != null) {
                Class activityClass = Class.forName(className);
                Intent intent = new Intent(currentActivity, activityClass);
                currentActivity.startActivity(intent);
            }
        } catch (Exception e) {

        }
    }
}
