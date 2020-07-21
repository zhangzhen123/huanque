package com.julun.rnlib.module;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
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

public class GlobalModule extends ReactContextBaseJavaModule {
    private static final String E_LAYOUT_ERROR = "E_LAYOUT_ERROR";

    public GlobalModule(ReactApplicationContext reactContext) {
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
            WritableMap map = Arguments.createMap();
            map.putMap("requestInfo", RnManager.INSTANCE.getHeaderInfo());
            map.putString("baseURL", CommonInit.Companion.getInstance().getBaseUrl());
            map.putString("encryptionKey", BusiConstant.API_KEY);
            promise.resolve(map);

        } catch (IllegalViewOperationException e) {
            e.printStackTrace();
            promise.reject(E_LAYOUT_ERROR, e);
        }
    }


    @ReactMethod
    public void sessionPast() {
        RnManager.INSTANCE.closeRnPager();
    }

    @ReactMethod
    public void avatarAuth(final Promise promise) {
        try {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    IRealNameService service = (IRealNameService) ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE).navigation();
                    service.startRealHead(getCurrentActivity(), new RealNameCallback() {
                        @Override
                        public void onCallback(@NotNull String status, @NotNull String des) {
                            if (status.equals(RealNameConstants.TYPE_SUCCESS)) {
                                promise.resolve(true);
                            } else {
                                ToastUtils.INSTANCE.show(des);
                                promise.resolve(false);
                            }
                        }
                    });

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            promise.reject(e);
        }

    }
}
