package com.julun.huanque.common.base.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.julun.huanque.common.R;


/**
 * 加载框
 * Created by djp on 2016/11/18.
 */

public class LoadingDialog extends ProgressDialog {
    private LayoutInflater layoutInflater;
    private View rootView;
    private TextView dialogMsg;

    public LoadingDialog(Context context) {
        this(context, R.style.Loading_Dialog);
    }

    public LoadingDialog(Context context, int theme) {
        super(context, theme);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        initView(context);
        setCanceledOnTouchOutside(false);
    }

    private void initView(Context context) {
        layoutInflater = LayoutInflater.from(context);
        rootView = layoutInflater.inflate(R.layout.dialog_loading, null);
        dialogMsg = (TextView) rootView.findViewById(R.id.dialogMsg);
    }

    public void showDialog(int resId, boolean bool) {
        showDialog(getContext().getString(resId), bool);
    }

    public void showDialog(String text) {
        dialogMsg.setText(text);
        setIndeterminate(true);
        setCancelable(true);
        show();
        setContentView(rootView);
    }

    public void showDialog(String text, boolean bool) {
        dialogMsg.setText(text);
        setIndeterminate(true);
        setCancelable(bool);
        show();
        setContentView(rootView);
    }

    public void showDialog(boolean bool) {
        showDialog(R.string.please_wait, bool);
    }

    public void showDialog() {
        showDialog(getContext().getString(R.string.please_wait));
    }

    public void showDialog(int resId) {
        showDialog(getContext().getString(resId));
    }
}
