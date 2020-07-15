package com.julun.huanque.message.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.MessageSettingBean
import com.julun.huanque.common.constant.ActivityCodes
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.NotificationUtils
import com.julun.huanque.message.R
import com.julun.huanque.message.fragment.PrivateChargeDialogFragment
import com.julun.huanque.message.viewmodel.MessageSettingViewModel
import kotlinx.android.synthetic.main.act_message_setting.*
import kotlinx.android.synthetic.main.act_message_setting.commonView
import kotlinx.android.synthetic.main.activity_live_remind.*
import org.jetbrains.anko.backgroundResource

/**
 *@创建者   dong
 *@创建时间 2020/6/30 11:26
 *@描述 消息设置页面
 */
class MessageSettingActivity : BaseActivity() {

    private var mViewModel: MessageSettingViewModel? = null

    private var mDialog: PrivateChargeDialogFragment? = null

    override fun getLayoutId() = R.layout.act_message_setting

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        findViewById<TextView>(R.id.tvTitle).text = "消息设置"
        prepareViewModel()
        mViewModel?.queryData?.value = true
        commonView.backgroundResource = R.color.transparent
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
        findViewById<View>(R.id.ivback).onClickNew { finish() }
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

        view_charge.onClickNew {
            //设置费用
            mDialog = mDialog ?: PrivateChargeDialogFragment()
            mDialog?.show(supportFragmentManager, "PrivateChargeDialogFragment")
        }
        iv_fold_stranger_msg.onClickNew {
            //折叠陌生人消息
            val sel = !iv_fold_stranger_msg.isSelected
            mViewModel?.updateSetting(foldMsg = if(sel){
                "True"
            }else{
                "False"
            })
        }
        iv_voice_communication.onClickNew {
            //接收语音通话
            val sel = !iv_voice_communication.isSelected
            mViewModel?.updateSetting(answer = if(sel){
                "True"
            }else{
                "False"
            })
        }
        iv_private_msg.onClickNew {
            //私信提醒
            val sel = !iv_private_msg.isSelected
            mViewModel?.updateSetting(privateMsgRemind = if(sel){
                "True"
            }else{
                "False"
            })
        }
        iv_attention.onClickNew {
            //关注提醒
            val sel = !iv_attention.isSelected
            mViewModel?.updateSetting(followRemind = if(sel){
                "True"
            }else{
                "False"
            })
        }
        view_start_live.onClickNew {
            //开播提醒
            startActivityForResult(LiveRemindActivity::class.java,ActivityCodes.REQUEST_CODE_NORMAL)
        }
    }

    private fun prepareViewModel() {
        mViewModel = ViewModelProvider(this).get(MessageSettingViewModel::class.java)

        mViewModel?.loadState?.observe(this, Observer {
            it?:return@Observer
            when(it.state){
                NetStateType.LOADING ->{
                    //加载中
                    commonView.showLoading("加载中~！")
                }
                NetStateType.SUCCESS ->{
                    //成功
                    commonView.hide()
                }
                NetStateType.IDLE->{
                    //闲置，什么都不做
                }
                else ->{
                    //都是异常
                    commonView.showError(errorTxt = "网络异常~！", btnClick = View.OnClickListener {
                        mViewModel?.queryData?.value = true
                    })
                }
            }
        })

        mViewModel?.settingResult?.observe(this, Observer {
            it ?: return@Observer
            commonView.hide()
            setViews(it)
        })
    }

    private fun setViews(info: MessageSettingBean) {
        tv_charge.text = if (info.privateMsgFee > 0) {
            "${info.privateMsgFee}/条"
        } else {
            "免费"
        }
        iv_fold_stranger_msg.isSelected = info.foldMsg
        iv_voice_communication.isSelected = info.answer
        iv_private_msg.isSelected = info.privateMsgRemind
        iv_attention.isSelected = info.followRemind
        tv_start_live.text = if (info.showRemindCnt > 0) {
            "已设置${info.showRemindCnt}位主播"
        } else {
            ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ActivityCodes.REQUEST_CODE_NORMAL -> {
                if (resultCode == ActivityCodes.RESPONSE_CODE_REFRESH) {
                    //通知刷新页面
                    mViewModel?.queryData?.value = true
                }
            }
        }
    }
}