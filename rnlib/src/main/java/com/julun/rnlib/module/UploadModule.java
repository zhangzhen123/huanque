package com.julun.rnlib.module;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.IllegalViewOperationException;
import com.julun.huanque.common.constant.ARouterConstant;
import com.julun.huanque.common.constant.BusiConstant;
import com.julun.huanque.common.constant.RealNameConstants;
import com.julun.huanque.common.init.CommonInit;
import com.julun.huanque.common.interfaces.routerservice.IRealNameService;
import com.julun.huanque.common.interfaces.routerservice.RealNameCallback;
import com.julun.huanque.common.utils.ToastUtils;
import com.julun.rnlib.RnManager;

import org.jetbrains.annotations.NotNull;

public class UploadModule extends ReactContextBaseJavaModule {
    private static final String E_LAYOUT_ERROR = "E_LAYOUT_ERROR";

    public UploadModule(ReactApplicationContext reactContext) {
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
    public void uploadPhotos(String rootPath, int max, Promise promise) {
        if (TextUtils.isEmpty(rootPath)) {
            promise.reject("-1", "图片上传功能 存储目录不能空 通知rn回调");
            return;
        }
        RnManager.INSTANCE.uploadPhotos(rootPath, max);
        RnManager.INSTANCE.getPromiseMap().put(RnManager.uploadPhotos, promise);

    }

    //{videoURL:'',imageURL:'',size:10000,time:100}
    @ReactMethod
    public void uploadVideos(ReadableMap params, Promise promise) {
        String rootPath = params.getString("rootPath");
        String imagePath = params.getString("imagePath");

        int maxSize = 0;
        if (!params.isNull("maxSize")) {
            maxSize = params.getInt("maxSize");
        }
        int minTime = 0;
        if (!params.isNull("minTime")) {
            minTime = params.getInt("minTime");
        }

        if (TextUtils.isEmpty(rootPath) || TextUtils.isEmpty(imagePath)) {
            promise.reject("-1", "视频上传功能 存储目录不能空 通知rn回调");
            return;
        }
        RnManager.INSTANCE.uploadVideo(rootPath, imagePath, maxSize, minTime);
        RnManager.INSTANCE.getPromiseMap().put(RnManager.uploadVideo, promise);

    }

}
