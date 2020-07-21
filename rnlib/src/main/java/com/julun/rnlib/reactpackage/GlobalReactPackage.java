package com.julun.rnlib.reactpackage;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.julun.rnlib.module.GlobalModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlobalReactPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(
            ReactApplicationContext reactContext) {
        return Arrays.<NativeModule>asList(new GlobalModule(reactContext));
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }
}