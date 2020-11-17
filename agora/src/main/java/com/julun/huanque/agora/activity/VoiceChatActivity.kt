package com.julun.huanque.agora.activity

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.julun.huanque.agora.R
import com.julun.huanque.common.agora.AgoraManager
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.ChatUser
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.RefreshVoiceCardEvent
import com.julun.huanque.common.bean.message.VoiceConmmunicationSimulate
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.VoiceFloatingManager
import com.julun.huanque.common.manager.VoiceManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.dp2pxf
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.PermissionUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.viewmodel.VoiceChatViewModel
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.agora.rtc.Constants
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.act_voice_chat.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.imageResource
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/7/2 14:23
 *@描述 1对1 语音页面
 */
@Route(path = ARouterConstant.VOICE_CHAT_ACTIVITY)
class VoiceChatActivity : BaseActivity() {
    private val TAG = "VoiceChatActivity"
    private val mVoiceChatViewModel = HuanViewModelManager.mVoiceChatViewModel

    //耳机是否插入
    private var mEarphone = false

    private var mCallingContentDisposable: Disposable? = null

    companion object {
        const val PERMISSIONALERT_WINDOW_CODE_VOICE = 0x1111
    }


    override fun getLayoutId() = R.layout.act_voice_chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, true)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        StatusBarUtil.setTransparent(this)
        initViewModel()
        mVoiceChatViewModel.waitingClose = false
        mEarphone = intent?.getBooleanExtra(ParamConstant.Earphone, false) ?: false
        ll_hands_free.isEnabled = !mEarphone
        if (intent?.getStringExtra(ParamConstant.From_Floating) != BusiConstant.True) {
            //不是从悬浮窗跳转过来
            //清空余额不足数据
            mVoiceChatViewModel.notEnoughData.value = null

            mVoiceChatViewModel.mType = intent?.getStringExtra(ParamConstant.TYPE) ?: ""
            val netCallBean = intent?.getSerializableExtra(ParamConstant.NetCallBean) as? NetcallBean
            if (netCallBean == null) {
                ToastUtils.show("没有对方数据")
                return
            }
            mVoiceChatViewModel?.netcallBeanData?.value = netCallBean
            mVoiceChatViewModel.voiceCardCount.value = netCallBean.ticketCnt

            if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLING) {
                //主叫
                mVoiceChatViewModel?.createUserId = SessionUtils.getUserId()
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_CALLING
            } else if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLED) {
                //被叫
                mVoiceChatViewModel?.createUserId = netCallBean.callerInfo.userId
                mVoiceChatViewModel?.currentVoiceState?.value = VoiceChatViewModel.VOICE_WAIT_ACCEPT
            }
            VoiceManager.playRing()
            if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLING) {
                checkPermissions()
                logger.info("Voice initViews = ${System.currentTimeMillis()}")
            } else {
                mVoiceChatViewModel.timer()
            }
            mVoiceChatViewModel.registerMessage()
        }



        waveView.setColor(Color.WHITE)
        waveView.setInitialRadius(dp2pxf(60))
        waveView.setMaxRadius(dp2pxf(105))
        waveView.setDuration(2500)
        waveView.setSpeed(750)
        waveView.setStyle(Paint.Style.STROKE)
        waveView.start()
    }


    override fun initEvents(rootView: View) {
        ll_voice_accept.onClickNew {
            //接收邀请
            checkPermissions()
        }

        ll_hands_free.onClickNew {
            //免提
            ll_hands_free.isSelected = !ll_hands_free.isSelected
            AgoraManager.mRtcEngine?.setEnableSpeakerphone(ll_hands_free.isSelected)
        }
        ll_quiet.onClickNew {
            //静音
            ll_quiet.isSelected = !ll_quiet.isSelected
            val volume = if (ll_quiet.isSelected) 0 else 100
            AgoraManager.mRtcEngine?.adjustRecordingSignalVolume(volume)
        }

        //adjustRecordingSignalVolume()
        ll_close.onClickNew {
            //挂断会话
//            if (mType == ConmmunicationUserType.CALLING) {
            //主叫
//                if (mVoiceChatViewModel?.currentVoiceState?.value == VoiceChatViewModel.VOICE_CALLING) {
//                    //手动取消
//                    mVoiceChatViewModel?.calcelVoice(CancelType.Normal)
//                }
            when (mVoiceChatViewModel?.currentVoiceState?.value) {
                VoiceChatViewModel.VOICE_CALLING -> {
                    //手动取消
                    mVoiceChatViewModel?.calcelVoice(CancelType.Normal)
                }
                VoiceChatViewModel.VOICE_ACCEPT -> {
                    //挂断通话
                    mVoiceChatViewModel?.hangUpVoice()
                }
                VoiceChatViewModel.VOICE_WAIT_ACCEPT -> {
                    //拒绝通话
                    mVoiceChatViewModel?.refuseVoice()
                }
                else -> {
                }
            }
//            } else if (mType == ConmmunicationUserType.CALLED) {
//                //被叫
//                mVoiceChatViewModel?.refuseVoice()
//            }
        }

        tv_recharge.onClickNew {
            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
        }

        iv_close.onClickNew {
            finish()
        }
        vew_voice_card.onClickNew {
            val bundle = Bundle()
            bundle.putBoolean("VOICE", true)
            bundle.putInt("TICKET_COUNT", mVoiceChatViewModel.voiceCardCount.value ?: 0)
            (ARouter.getInstance().build(ARouterConstant.PROP_FRAGMENT).with(bundle)
                .navigation() as? BaseDialogFragment)?.show(supportFragmentManager, "PropFragment")
        }
    }


    /**
     * 检查权限
     */
    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(Manifest.permission.RECORD_AUDIO)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLING) {
                            mVoiceChatViewModel.timer()
                            logger.info("Voice checkPermissions = ${System.currentTimeMillis()}")
                        } else if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLED) {
                            mVoiceChatViewModel?.acceptVoice()
                            //加入会话
                            mVoiceChatViewModel.joinChannel()
                        }
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger.info("获取权限被永久拒绝")
                        val message = "无法获取到相机/存储权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        mVoiceChatViewModel?.currentVoiceState?.observe(this, Observer {
            if (it != null) {
                when (it) {
                    VoiceChatViewModel.VOICE_CALLING -> {
                        //呼叫状态
                        ll_quiet.hide()
                        ll_close.show()
                        ll_hands_free.hide()
                        ll_voice_accept.hide()
                        showWaitContent(true)

                    }
                    VoiceChatViewModel.VOICE_WAIT_ACCEPT -> {
                        //待接听状态
                        tv_voice_cancel.text = "挂断"
                        ll_quiet.hide()
                        ll_close.show()
                        ll_hands_free.hide()
                        ll_voice_accept.show()
                        showWaitContent(false)
                    }
                    VoiceChatViewModel.VOICE_ACCEPT -> {
                        //接听状态
                        tv_voice_cancel.text = "挂断"
                        ll_quiet.show()
                        ll_close.show()
                        ll_hands_free.show()
//                        ll_hands_free.isEnabled = !GlobalUtils.getEarphoneLinkStatus()
                        ll_voice_accept.hide()

                        mCallingContentDisposable?.dispose()
                        //开始对方未加入倒计时
                        mVoiceChatViewModel.otherNoJoinCountDown()
                    }
                    VoiceChatViewModel.VOICE_CLOSE -> {
                        //结束状态
                        //退出频道
//                        leaveChannel()
//                        ToastUtils.show("通话已结束")
                        mVoiceChatViewModel.mOtherNoJoinDisposable?.dispose()
                        mDurationDisposable?.dispose()
                        mCallingContentDisposable?.dispose()
                        Observable.timer(1, TimeUnit.SECONDS)
                            .bindUntilEvent(this, ActivityEvent.DESTROY)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ finish() }, {}, {})
//                        ll_quiet.show()
//                        ll_close.show()
//                        ll_hands_free.show()
//                        ll_voice_accept.hide()
                    }
                }
            }
        })
        mVoiceChatViewModel.targetUserBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })

        mVoiceChatViewModel.netcallBeanData.observe(this, Observer { netCallBean ->
            if (netCallBean != null) {
                if (netCallBean.beans > 0) {
                    tv_fee.show()
                    tv_fee.text = "语音通话${netCallBean.beans}鹊币/分钟"
                }
                if (netCallBean.unconfirmed) {
                    //未确认付费，需要显示弹窗
                    mVoiceChatViewModel?.markFeeRemind()

                    CommonInit.getInstance().getCurrentActivity()?.let { act ->
                        MyAlertDialog(act).showAlertWithOKAndCancel(
                            "语音通话${netCallBean.beans}鹊币/分钟",
                            MyAlertDialog.MyDialogCallback(onRight = {
                                SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                            }, onCancel = {
                                SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_FEE_DIALOG_SHOW, true)
                            }), "语音通话费用", "发起通话"
                        )
                    }
                }
                var backPic = ""
                if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLING) {
                    //主叫
                    mVoiceChatViewModel?.targetUserBean?.value = netCallBean.receiverInfo
                    backPic = netCallBean.receiverInfo.backPic
                } else if (mVoiceChatViewModel.mType == ConmmunicationUserType.CALLED) {
                    mVoiceChatViewModel?.targetUserBean?.value = netCallBean.callerInfo
                    backPic = netCallBean.callerInfo.backPic
                }
                mVoiceChatViewModel?.agoraToken = netCallBean.token
                mVoiceChatViewModel?.channelId = netCallBean.channelId
                mVoiceChatViewModel?.callId = netCallBean.callId
                mVoiceChatViewModel?.duration.value = 0
                if (netCallBean.beans > 0) {
                    //付费方
                    tv_price.show()
                    tv_price.text = "${netCallBean.beans}鹊币/分钟"
                }

                ImageUtils.loadImageWithBlur(
                    sdv_bg, backPic, 4, 4, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight()
                )
            }
        })

        mVoiceChatViewModel.voiceCardCount.observe(this, Observer {
            if (it != null) {
                tv_voice_card.text = "剩余：$it"
//                if (it > 0) {
////                    vew_voice_card.show()
////                    iv_voice_card.show()
////                    tv_voice_card.show()
//                    tv_voice_card.text = "剩余：$it"
//                } else {
////                    vew_voice_card.hide()
////                    iv_voice_card.hide()
////                    tv_voice_card.hide()
//                }
            }
        })

        mVoiceChatViewModel.handsFreeState.observe(this, Observer {
            if (it != null) {
                ll_hands_free.isEnabled = it
                mVoiceChatViewModel.handsFreeState.value = null
            }
        })
        mVoiceChatViewModel.updateDurationParamsFlag.observe(this, Observer {
            if (it != null) {
                updataDurationParams(it)
            }
        })
        mVoiceChatViewModel.duration.observe(this, Observer {
            if (it != null) {
                tv_call_duration.text = "通话时长：${TimeUtils.countDownTimeFormat1(it)}"
            }
        })
        mVoiceChatViewModel.notEnoughData.observe(this, Observer {
            if (it != null) {
                showBalanceNotEnoughView(it)
            }
        })

        mVoiceChatViewModel.voiceCardDuration.observe(this, Observer {
            if (it != null) {
                //语音卡倒计时  剩余时间
                if (it > 0) {
                    bg_voice_time.show()
                    tv_voice_time_attention.show()
                    tv_voice_time.show()
                    tv_voice_time.text = "${it}秒"
                } else {
                    bg_voice_time.hide()
                    tv_voice_time_attention.hide()
                    tv_voice_time.hide()
                }
            }
        })
    }


    /**
     * 循环显示呼叫状态
     * @param calling  true ： 拨号状态   false : 带接听状态
     */
    private fun showWaitContent(calling: Boolean) {
        val totalContent = if (calling) {
            "正在呼叫对方，等待接通..."
        } else {
            "正在邀请您语音通话..."
        }
        updataDurationParams(false, totalContent)

        mCallingContentDisposable?.dispose()
        mCallingContentDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val index = it % 3
                when (index) {
                    0L -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通."
                        } else {
                            "正在邀请您语音通话."
                        }
                    }
                    1L -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通.."
                        } else {
                            "正在邀请您语音通话.."
                        }
                    }
                    else -> {
                        tv_call_duration.text = if (calling) {
                            "正在呼叫对方，等待接通..."
                        } else {
                            "正在邀请您语音通话..."
                        }
                    }
                }

            }, { it.printStackTrace() })

    }

    /**
     * 设置时长的布局
     * @param duration 是否显示通话时长  true 通话时长   false  呼叫状态
     * @param content 呼叫状态的最长文案
     */
    private fun updataDurationParams(duration: Boolean, content: String = "") {
        val params = tv_call_duration.layoutParams as? ConstraintLayout.LayoutParams
        if (duration) {
            //通话样式
            params?.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
        } else {
            //呼叫样式
            val paint = tv_call_duration.paint
            val width = paint.measureText(content)
            params?.width = width.toInt()
        }
        tv_call_duration.layoutParams = params

    }


    /**
     * 显示页面
     */
    private fun showViewByData(bean: ChatUser) {
        ImageUtils.loadImage(sdv_header, bean.headPic, 100f, 100f)
        tv_nickname.text = bean.nickname
        val sexImage = when (bean.sex) {
            Sex.MALE -> {
                R.mipmap.icon_voice_male
            }
            Sex.FEMALE -> {
                R.mipmap.icon_voice_female
            }
            else -> {
                0
            }
        }
        if (sexImage > 0) {
            iv_sex.imageResource = sexImage
        }
    }


    //余额不足，剩余时间使用的定时器
    private var mDurationDisposable: Disposable? = null

    /**
     * 显示余额不足弹窗
     */
    private fun showBalanceNotEnoughView(bean: NetCallBalanceRemindBean) {
        if (bean.enough || (mVoiceChatViewModel.notEnoughData.value?.duration ?: 0) == 0L) {
            //隐藏视图
            mDurationDisposable?.dispose()
            view_balance.hide()
            tv_recharge.hide()
            tv_attention.hide()
            tv_surplus_time.hide()
        } else {
            //显示视图
            view_balance.show()
            tv_recharge.show()
            tv_attention.show()
            tv_surplus_time.show()

            mDurationDisposable?.dispose()
            val duration = bean.duration
            mDurationDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(duration + 1)
                .map {
                    mVoiceChatViewModel.notEnoughData.value?.duration = duration - it
                    "${duration - it}"
                }
                .bindUntilEvent(this, ActivityEvent.DESTROY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_surplus_time.text = "${it}秒"
                }, { it.printStackTrace() })
        }
    }


    override fun onViewDestroy() {
        super.onViewDestroy()
        waveView.stopImmediately()
    }

    override fun onDestroy() {
        super.onDestroy()
        //发送事件，通知刷新语音券
        EventBus.getDefault().post(RefreshVoiceCardEvent())
        if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
            //正在通话中，显示悬浮窗
            if (PermissionUtils.checkFloatPermission(this)) {
                //显示悬浮窗
                VoiceFloatingManager.showFloatingView()
            }
        }
    }


    override fun onBackPressed() {
        if (PermissionUtils.checkFloatPermission(this)) {
            super.onBackPressed()
        } else {
            showPermissionDialog()
        }
    }

    override fun finish() {
        if (PermissionUtils.checkFloatPermission(this)) {
            super.finish()
        } else {
            showPermissionDialog()
        }
    }

    /**
     * 显示授权提醒弹窗
     */
    private fun showPermissionDialog() {
        MyAlertDialog(this).showAlertWithOKAndCancel(
            "悬浮窗权限被禁用，请到设置中授予欢鹊悬浮窗权限",
            MyAlertDialog.MyDialogCallback(onRight = {
                val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
                intent.data = Uri.parse("package:$packageName")
//                startActivityForResult(intent, PERMISSIONALERT_WINDOW_CODE_VOICE)
                startActivity(intent)
            }), "设置提醒", "去设置"
        )
    }


}