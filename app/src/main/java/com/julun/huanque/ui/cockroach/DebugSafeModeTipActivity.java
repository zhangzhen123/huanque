package com.julun.huanque.ui.cockroach;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.julun.huanque.R;


/**
 * Created by wanjian on 2018/5/21.
 */

public class DebugSafeModeTipActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_mode_warning);

        findViewById(R.id.log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(CrashLogFragment.class.getName());
                if (fragment == null) {
                    fragment = new CrashLogFragment();
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, fragment, CrashLogFragment.class.getName())
                        .commit();
            }
        });
    }


}
