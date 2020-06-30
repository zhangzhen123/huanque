package com.julun.huanque.message.activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import com.effective.android.panel.PanelSwitchHelper
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.onTouch
import kotlinx.android.synthetic.main.act_private_chat.*


/**
 *@创建者   dong
 *@创建时间 2020/6/30 16:55
 *@描述 私聊详情页
 */
class PrivateChatActivity : BaseActivity() {

    private var mHelper: PanelSwitchHelper? = null

    override fun getLayoutId() = R.layout.act_private_chat

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

    }

    override fun initEvents(rootView: View) {
        findViewById<View>(R.id.ivback).onClickNew {
            finish()
        }

        bottom_action.onTouch { v, event ->
            return@onTouch true
        }
    }

    override fun onStart() {
        super.onStart()
        if (mHelper == null) {
            mHelper = PanelSwitchHelper.Builder(this)
                .addKeyboardStateListener {
                    onKeyboardChange { visible, height ->
                        //可选实现，监听输入法变化
                    }
                }
                .addEditTextFocusChangeListener {
                    onFocusChange { _, hasFocus ->
                        //可选实现，监听输入框焦点变化
                    }
                }
                .addViewClickListener {
                    onClickBefore {
                        //可选实现，监听触发器的点击
                    }
                }
                .addPanelChangeListener {
                    onKeyboard {
                        //可选实现，输入法显示回调
                    }
                    onNone {
                        //可选实现，默认状态回调
                    }
                    onPanel {
                        //可选实现，面板显示回调
                    }
                    onPanelSizeChange { panelView, _, _, _, width, height ->
                        //可选实现，输入法动态调整时引起的面板高度变化动态回调
                    }
                }
                .contentCanScrollOutside(true)    //可选模式，默认true，当面板实现时内容区域是否往上滑动
                .logTrack(true)                   //可选，默认false，是否开启log信息输出
                .build(false)                      //可选，默认false，是否默认打开输入法
        }
    }


    override fun onBackPressed() {
        //用户按下返回键的时候，如果显示面板，则需要隐藏
        if (mHelper != null && mHelper?.hookSystemBackByPanelSwitcher() == true) {
            return;
        }
        super.onBackPressed();
    }
}