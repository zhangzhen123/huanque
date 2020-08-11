package com.julun.rnlib;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.fresco.FrescoModule;
import com.facebook.react.shell.MainReactPackage;
import com.julun.huanque.common.init.CommonInit;

public class MyMainReactPackage extends MainReactPackage {
    @Nullable
    @Override
    public NativeModule getModule(String name, ReactApplicationContext context) {
        if(name.equals(FrescoModule.NAME)){
            return new FrescoModule(context, false, CommonInit.Companion.getInstance().getFrescoConfig());
        }
        return super.getModule(name, context);
    }
}
