package com.julun.huanque.common.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.julun.huanque.common.R;


/**
 * 自定义样式屏幕中间显示提示消息
 * Created by djp on 2016/11/18.
 *
 * @author djp
 */

public class ToastUtils {
    public static void init(Context context) {
        ToastMsg.INSTANCE.init(context.getApplicationContext());
    }

    public static void show(int resId) {
        ToastMsg.INSTANCE.showToast(resId, 0);
    }

    public static void show(String text) {
        ToastMsg.INSTANCE.showToast(text, Toast.LENGTH_SHORT);
    }

    public static void showLong(String text) {
        ToastMsg.INSTANCE.showToast(text, Toast.LENGTH_LONG);
    }

    public static void show(String text, int duration) {
        ToastMsg.INSTANCE.showToast(text, duration);
    }

    public static void showSuccessMessage(String text) {
        ToastMsg.INSTANCE.showSuccessToast(text);
    }

    public static void showErrorMessage(String text) {
        ToastMsg.INSTANCE.showErrorToast(text);
    }

    public static void showCustom(String text, int duration, int gravity) {
        ToastMsg.INSTANCE.showToastCustom(text, duration, gravity);
    }

    public enum ToastMsg {
        /**
         * ToastMsg对象
         */
        INSTANCE;

        private Toast toast;
        private View view;
        private TextView tvContent;
        private ImageView customImage;

        private void init(Context context) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
            tvContent = (TextView) view.findViewById(R.id.toastContent);
            customImage = (ImageView) view.findViewById(R.id.customImage);
            if (toast == null) {
                toast = new Toast(context);
            }
            toast.setView(view);
            toast.setGravity(Gravity.CENTER, 0, 0);
        }

        public void showSuccessToast(CharSequence text) {
            this.customImage.setVisibility(View.VISIBLE);
            this.customImage.setImageResource(R.mipmap.toast_success);
            this.tvContent.setText(text);
            toast.setGravity(Gravity.CENTER, 0, 0);
            this.toast.show();
        }

        public void showErrorToast(CharSequence text) {
            this.customImage.setVisibility(View.VISIBLE);
            this.customImage.setImageResource(R.mipmap.toast_error);
            this.tvContent.setText(text);
            toast.setGravity(Gravity.CENTER, 0, 0);
            this.toast.show();
        }

        public void showToast(int resId) {
            if (resId != 0) {
                this.customImage.setVisibility(View.GONE);
                this.tvContent.setText(resId);
                toast.setGravity(Gravity.CENTER, 0, 0);
                this.toast.show();
            }
        }

        public void showToast(int resId, int duration) {
            if (resId != 0) {
                this.customImage.setVisibility(View.GONE);
                this.tvContent.setText(resId);
                this.toast.setDuration(duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                this.toast.show();
            }
        }

        public void showToast(CharSequence text) {
            if (!TextUtils.isEmpty(text)) {
                this.customImage.setVisibility(View.GONE);
                this.tvContent.setText(text);
                toast.setGravity(Gravity.CENTER, 0, 0);
                this.toast.show();
            }
        }

        public void showToast(CharSequence text, int duration) {
            if (!TextUtils.isEmpty(text)) {
                this.customImage.setVisibility(View.GONE);
                this.tvContent.setText(text);
                this.toast.setDuration(duration);
                toast.setGravity(Gravity.CENTER, 0, 0);
                this.toast.show();
            }
        }

        public void showToastCustom(CharSequence text, int duration, int gravity) {
            if (!TextUtils.isEmpty(text)) {
                this.customImage.setVisibility(View.GONE);
                this.tvContent.setText(text);
                this.toast.setDuration(duration);
                this.toast.setGravity(gravity, 0, DensityUtils.dp2px(60f));
                this.toast.show();
            }
        }
    }

}
