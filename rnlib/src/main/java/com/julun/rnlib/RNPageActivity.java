package com.julun.rnlib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.BV.LinearGradient.LinearGradientPackage;
import com.facebook.infer.annotation.Assertions;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.julun.rnlib.reactpackage.OpenActivityReactPackage;
import com.julun.rnlib.reactpackage.RequestInfoReactPackage;
import com.julun.rnlib.reactpackage.ToastReactPackage;


import androidx.appcompat.app.AppCompatActivity;
import android.view.KeyEvent;

// Android打开RN页面
public class RNPageActivity extends AppCompatActivity implements DefaultHardwareBackBtnHandler {
    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;
    private boolean mDeveloperSupport = true;
    private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer;

    public static void start(Activity activity, String appName) {
        Intent intent = new Intent(activity, RNPageActivity.class);
        intent.putExtra("appName", appName);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                // 背景色渐变：需放到application中
                .addPackage(new LinearGradientPackage())
                // 请求头数据：需放到application中
                .addPackage(new RequestInfoReactPackage())
                .addPackage(new ToastReactPackage())
                .addPackage(new OpenActivityReactPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.BEFORE_CREATE)
                .build();

        Intent intent = getIntent();
        String appName = intent.getStringExtra("appName");

        // 这个"App1"名字一定要和我们在index.js中注册的名字保持一致AppRegistry.registerComponent()
        mReactRootView.startReactApplication(mReactInstanceManager, appName, null);

        mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();
        setContentView(mReactRootView);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getUseDeveloperSupport()) {
            if (keyCode == KeyEvent.KEYCODE_MENU) {//Ctrl + M 打开RN开发者菜单
                mReactInstanceManager.showDevOptionsDialog();
                return true;
            }
            boolean didDoubleTapR = Assertions.assertNotNull(mDoubleTapReloadRecognizer).didDoubleTapR(keyCode, getCurrentFocus());
            if (didDoubleTapR) {//双击R 重新加载JS
                mReactInstanceManager.getDevSupportManager().handleReloadJS();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean getUseDeveloperSupport() {
        return mReactInstanceManager != null && mDeveloperSupport;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    public void onBackPressed() {
        if (mReactInstanceManager != null) {
            mReactInstanceManager.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
        }
    }
}
