package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;

import java.util.Date;

public class RequestInfoModule extends ReactContextBaseJavaModule {
    private static final String E_LAYOUT_ERROR = "E_LAYOUT_ERROR";

    public RequestInfoModule(ReactApplicationContext reactContext) {
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
    public void getRequestInfo(Promise promise) {
        try {
            WritableMap headerMap = Arguments.createMap();
            headerMap.putString("s", "111");
            headerMap.putString("t", "222");
            headerMap.putString("s", "1.0.0");
            headerMap.putDouble("h", new Date().getTime());


            WritableMap map = Arguments.createMap();
            map.putMap("headerInfo", headerMap);
            map.putString("baseURL", "https://api.51lm.tv/");

            promise.resolve(map);
        } catch (IllegalViewOperationException e) {
            e.printStackTrace();
            promise.reject(E_LAYOUT_ERROR, e);
        }
    }
}
