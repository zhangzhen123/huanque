package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.julun.huanque.common.constant.BusiConstant;
import com.julun.huanque.common.init.CommonInit;
import com.julun.rnlib.RnManager;

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
//            WritableMap headerMap = Arguments.createMap();
//            headerMap.putString("s", "111");
//            headerMap.putString("t", "222");
//            headerMap.putString("s", "1.0.0");
//            headerMap.putDouble("h", new Date().getTime());
//
//            Map<String, String> headerInfo = HeaderInfoHelper.INSTANCE.getMobileDeviceInfo();
            WritableMap map = Arguments.createMap();
            map.putMap("headerInfo", RnManager.INSTANCE.getHeaderInfo());
            map.putString("baseURL", CommonInit.Companion.getInstance().getBaseUrl());
            map.putString("encryptionKey", BusiConstant.API_KEY);
            promise.resolve(map);

        } catch (IllegalViewOperationException e) {
            e.printStackTrace();
            promise.reject(E_LAYOUT_ERROR, e);
        }
    }
    @ReactMethod
    public void uploadPhotos(int max,Promise promise){
        RnManager.INSTANCE.uploadPhotos(max);
        RnManager.promiseMap.put(RnManager.uploadPhotos,promise);

    }
    @ReactMethod
    public void  sessionPast() {
        RnManager.INSTANCE.closeRnPager();
    }

}
