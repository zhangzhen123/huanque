package com.julun.huanque.message.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.julun.huanque.R
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.NotificationUtils
import kotlinx.android.synthetic.main.act_message_setting.*

/**
 *@创建者   dong
 *@创建时间 2020/6/30 11:26
 *@描述 消息设置页面
 */
class MessageSettingActivity : BaseActivity() {
    override fun getLayoutId() = R.layout.act_message_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "消息设置"

    }

    override fun onResume() {
        super.onResume()
        val open = NotificationUtils.areNotificationsEnabled(this)
        tv_notification_status.text = if (open) {
            "已开启"
        } else {
            "未开启"
        }
    }

    override fun initEvents(rootView: View) {
        view_notification.onClickNew {
            val intent = NotificationUtils.gotoNotificationSetting(this)
            if (!TextUtils.isEmpty(intent.action) && ForceUtils.activityMatch(intent)) {
                try {
                    //配置action成功才跳转
                    startActivityForResult(intent, BusiConstant.NOTIFICATION_REQUEST_CODE)
                } catch (e: Exception) {
                    reportCrash("跳转通知页失败", e)
                }
            }
        }
    }
}