package com.julun.rnlib.module;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.alibaba.android.arouter.launcher.ARouter;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.julun.huanque.common.constant.ARouterConstant;
import com.julun.huanque.common.constant.OperationType;
import com.julun.huanque.common.constant.ParamConstant;
import com.julun.huanque.common.utils.ULog;

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
            switch (pageName) {
                case "PrivateMessagePage": {
                    String id = params.getString("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ParamConstant.TARGETID, userId);
//                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation(getCurrentActivity());
                    }

                    break;
                }
                case "TelephoneCallPage": {
                    String id = params.getString("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ParamConstant.TARGETID, userId);
//                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
                        bundle.putString(ParamConstant.OPERATION, OperationType.CALL_PHONE);
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation(getCurrentActivity());
                    }
                    break;
                }
                case "SendGiftPage": {
                    String id = params.getString("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle bundle = new Bundle();
                        bundle.putLong(ParamConstant.TARGETID, userId);
//                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
                        bundle.putString(ParamConstant.OPERATION, OperationType.OPEN_GIFT);
                        ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation(getCurrentActivity());
                    }
                    break;
                }
                case "RecordVoicePage": {

                    break;
                }
                case "ReportUserPage":{
                    String id = params.getString("userId");
                    if (id != null) {
                        long userId = Long.parseLong(id);
                        Bundle extra = new Bundle();
                        extra.putLong(ParamConstant.TARGET_USER_ID, userId);
                        extra.putInt(ParamConstant.REPORT_TYPE, 0);
                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra).navigation();
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
