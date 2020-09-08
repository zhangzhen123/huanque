package com.julun.huanque.agora.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.RESTART
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.agora.AgoraManager
import com.julun.huanque.agora.R
import com.julun.huanque.agora.handler.EventHandler
import com.julun.huanque.agora.viewmodel.AnonymousVoiceViewModel
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.HideFloatingEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.manager.VoiceManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.agora.rtc.Constants
import io.agora.rtc.IRtcEngineEventHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.internal.operators.observable.ObservableTake
import kotlinx.android.synthetic.main.act_anonymous_voice.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.textColor
import java.util.concurrent.TimeUnit

/**
 *@创建者   dong
 *@创建时间 2020/8/18 16:08
 *@描述 匿名语音页面
 */
@Route(path = ARouterConstant.ANONYMOUS_VOICE_ACTIVITY)
class AnonymousVoiceActivity : BaseActivity(), EventHandler {
    override fun getLayoutId() = R.layout.act_anonymous_voice

    //匹配时长的计时器
    private var mDisposable: Disposable? = null

    //匹配动画，第一个动画
    private val animatorFirstSet = AnimatorSet()

    //匹配动画，第二个动画
    private val animatorSecondSet = AnimatorSet()

    //左侧头像缩放动画
    private val leftHeaderAnimatorSet = AnimatorSet()
    private val topHeaderAnimatorSet = AnimatorSet()
    private val rightHeaderAnimatorSet = AnimatorSet()

    private val voiceShowAnimatorSet = AnimatorSet()
    private val showBtnAnimatorSet = AnimatorSet()

    //显示本人数据的动画
    private val showMineInfoAnimatorSet = AnimatorSet()

    //显示对方数据的动画
    private val showOtherInfoAnimatorSet = AnimatorSet()

    //匹配过程中
    private val matchCompositeDisposable = CompositeDisposable()


    //通话过程中
    private val voiceCompositeDisposable = CompositeDisposable()

    private var mAnonymousVoiceViewModel: AnonymousVoiceViewModel? = null

    //匹配从多少时间开始
    private var mMatchStartTime = 0L

    //通话时长
    private var communicationTime = 0L

    private var am: AudioManager? = null

    //其他人未加入倒计时
    private var mOtherNoJoinDisposable: Disposable? = null

    //语音结束关闭标识位
    private var endClose = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().post(HideFloatingEvent())
        ll_hands_free.isEnabled = !GlobalUtils.getEarphoneLinkStatus()
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        StatusBarUtil.setTransparent(this)

        val barHeight = StatusBarUtil.getStatusBarHeight(this)
        val params = header_page.layoutParams as? ConstraintLayout.LayoutParams
        params?.topMargin = barHeight
        header_page.layoutParams = params

        AgoraManager.mHandler.addHandler(this)
        header_page.textTitle.text = "匿名语音"
        header_page.textTitle.textColor = Color.WHITE
        header_page.imageViewBack.imageResource = R.mipmap.icon_back_white_01

        con_match.show()
        con_voice.hide()

        ImageUtils.loadImage(sdv_header, "${SessionUtils.getHeaderPic()}${BusiConstant.OSS_160}", 75f, 75f)


        initViewModel()
        registerMessage()

        judgeType(intent)
        mAnonymousVoiceViewModel?.getBasicData()
    }

    /**
     * 跳转类型进行判断
     */
    private fun judgeType(intent: Intent?) {
        val mType = intent?.getStringExtra(ParamConstant.TYPE) ?: ""
        val currentType = mAnonymousVoiceViewModel?.currentState?.value
        if ((currentType == null || currentType == AnonymousVoiceViewModel.WAIT) && mType == ConmmunicationUserType.CALLED) {
            //被叫
            mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.WAIT_ACCEPT
            mAnonymousVoiceViewModel?.inviteUserId = intent?.getLongExtra(ParamConstant.InviteUserId, 0) ?: 0L
            //1 播放响铃
            playAudio(true)
            //2 超时计算
            timer()
        } else {
            //主叫
            endClose = false
            mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.WAIT
        }
    }


    private fun registerMessage() {
        MessageProcessor.clearProcessors(false)
        //匹配超时消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceTimeoutProcessor {
            override fun process(data: UserIdListBean) {
                if (data.userIds.contains(SessionUtils.getUserId())) {
                    //当前用户的匹配超时
                    ToastUtils.show("还没有人配的上你，晚点再来试试吧")
                    mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.WAIT
                }
            }
        })
        //匹配成功消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceConnectProcessor {
            override fun process(data: AnonyVoiceSuccess) {
                if (data.userIds.contains(SessionUtils.getUserId())) {
                    //当前用户匹配成功
                    communicationTime = data.duration
                    val userInfo = data.userData["${SessionUtils.getUserId()}"] ?: return
                    //更新剩余次数
                    mAnonymousVoiceViewModel?.basicData?.value?.surplusTimes = userInfo.surplusTimes

                    mAnonymousVoiceViewModel?.agoraToken = userInfo.token
                    mAnonymousVoiceViewModel?.channelId = data.channelId
                    mAnonymousVoiceViewModel?.callId = data.callId

                    mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.VOICE
                }
            }

        })

        //匿名语音挂断消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceHangUpProcessor {
            override fun process(data: AnonyVoiceHangUpBean) {
                if (data.callId == mAnonymousVoiceViewModel?.callId && data.userId == SessionUtils.getUserId()) {
                    //当前匿名语音被挂断
                    mAnonymousVoiceViewModel?.voiceEndFlag?.value = true
                }
            }
        })

        //公开身份消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceOpenProcessor {
            override fun process(data: UserInfoInRoom) {
                showUserInfo(data)
            }
        })
        //揭秘身份消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceUnveilProcessor {
            override fun process(data: UserInfoInRoom) {
                showUserInfo(data)
            }
        })

        MessageProcessor.registerEventProcessor(object : MessageProcessor.AnonyVoiceCancelProcessor {
            override fun process(data: AnonyVoiceCancelBean) {
                if (data.inviteUserId == mAnonymousVoiceViewModel?.inviteUserId && mAnonymousVoiceViewModel?.currentState?.value == AnonymousVoiceViewModel.WAIT_ACCEPT) {
                    //匿名语音取消消息
                    ToastUtils.show("匿名语音已结束")
                    mAnonymousVoiceViewModel?.voiceEndFlag?.value = true
                }
            }
        })

    }

    /**
     * 被叫 的等待定时挂断方法
     */
    private fun timer() {
        //超时计算
        mDisposable = Observable.timer(30, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .subscribe({
                //挂断
                ToastUtils.show("匿名语音已结束")
                mAnonymousVoiceViewModel?.currentState?.postValue(AnonymousVoiceViewModel.WAIT)
//                mAnonymousVoiceViewModel?.hangUp()
            }, {})
    }

    /**
     * 播放音效
     */
    private fun playAudio(calling: Boolean) {
        if (calling) {
            if (mAnonymousVoiceViewModel?.currentState?.value == AnonymousVoiceViewModel.MATCH) {
                VoiceManager.startMatch()
            } else {
                VoiceManager.startRing()
            }
        } else {
            VoiceManager.startFinish()
        }

        am = am ?: getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        am?.mode = AudioManager.MODE_NORMAL
        am?.isSpeakerphoneOn = true
    }

    private fun initViewModel() {
        mAnonymousVoiceViewModel = ViewModelProvider(this).get(AnonymousVoiceViewModel::class.java)
        mAnonymousVoiceViewModel?.basicData?.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }
        })
        mAnonymousVoiceViewModel?.currentState?.observe(this, Observer {
            if (it != null) {
                VoiceManager.stopAllVoice()
                when (it) {
                    AnonymousVoiceViewModel.WAIT -> {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
                        mDisposable?.dispose()

                        stopMatch()
                        con_voice.hide()
                        con_match.show()
                        leaveChannel()
                        header_page.imageViewBack.show()
                        voiceCompositeDisposable.clear()
                    }
                    AnonymousVoiceViewModel.MATCH -> {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, true)
                        playAudio(true)
                        startMatch()
                    }
                    AnonymousVoiceViewModel.VOICE -> {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, true)
                        showMatchSuccessView()
                        startVoiceCountDown()
                        joinChannel()
                        otherNoJoinCountDown()
                    }
                    AnonymousVoiceViewModel.WAIT_ACCEPT -> {
                        //等待接听状态
                        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, true)
                        showMatchSuccessView()
                        showWaitAccept()
                    }
                    else -> {
                    }
                }
            }
        })

        mAnonymousVoiceViewModel?.showMineInfoData?.observe(this, Observer {
            if (it != null) {
                showUserInfo(it)
            }
        })

        mAnonymousVoiceViewModel?.checkBeansData?.observe(this, Observer {
            if (it != null) {
                val enough = it.hasEnoughBeans == BusiConstant.True
                val okText = if (enough) {
                    "确认"
                } else {
                    "去充值"
                }
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "您需要支付${it.beans}鹊币才能查看对方的真实身份哦~",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        if (enough) {
                            //去揭秘
                            mAnonymousVoiceViewModel?.unveilIdentity()
                        } else {
                            //去充值
                            ARouter.getInstance().build(ARouterConstant.RECHARGE_ACTIVITY).navigation()
                        }

                    }, onCancel = {
                        SharedPreferencesUtils.commitBoolean(SPParamKey.MESSAGE_FEE_DIALOG_SHOW, true)
                    }), "揭秘对方身份", okText, noText = "取消"
                )
            }
        })

        mAnonymousVoiceViewModel?.unveilIdentityData?.observe(this, Observer {
            if (it != null) {
                showUserInfo(it)
            }
        })

        mAnonymousVoiceViewModel?.closeSoundFlag?.observe(this, Observer {
            if (it == true) {
                playAudio(false)
            }
        })

        mAnonymousVoiceViewModel?.voiceEndFlag?.observe(this, Observer {
            if (it == true) {
                //匿名语音结束标记位
                if (endClose) {
                    Observable.timer(1, TimeUnit.SECONDS)
                        .bindUntilEvent(this, ActivityEvent.DESTROY)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            finish()
                        }, {})
                } else {
                    mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.WAIT
                }
            }
        })

        mAnonymousVoiceViewModel?.followStatusData?.observe(this, Observer {
            if (it != null) {
                if (it.userId == mAnonymousVoiceViewModel?.targetUserId) {
                    iv_follow.hide()
                }
            }
        })
    }

    private fun showViewByData(info: AnonymousBasicInfo) {
        ImageUtils.loadImage(sdv_header, "${info.myHeadPic}${BusiConstant.OSS_160}", 75f, 75f)
        val count = info.surplusTimes
        tv_match.isEnabled = count > 0
        tv_duration.text = "剩余${info.surplusTimes}次"
        info.headPics.forEachIndexed { index, header ->
            when (index) {
                0 -> {
                    sdv_left.loadImage("${header}${BusiConstant.OSS_120}", 60f, 60f)
                }
                1 -> {
                    sdv_top.loadImage("${header}${BusiConstant.OSS_120}", 60f, 60f)
                }
                2 -> {
                    sdv_right.loadImage("${header}${BusiConstant.OSS_120}", 60f, 60f)
                }
            }
        }
        mMatchStartTime = info.waitingSeconds
        if (info.waitingSeconds > 0) {
            //直接进入匹配状态
            mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.MATCH
        }
    }

    override fun initEvents(rootView: View) {
        ll_hands_free.onClickNew {
            //免提
            ll_hands_free.isSelected = !ll_hands_free.isSelected
            AgoraManager.mRtcEngine?.setEnableSpeakerphone(ll_hands_free.isSelected)
        }
        ll_close.onClickNew {
            //挂断
            if (mAnonymousVoiceViewModel?.currentState?.value == AnonymousVoiceViewModel.WAIT_ACCEPT) {
                //拒绝语音邀请
                mAnonymousVoiceViewModel?.inviteUserId?.let { id ->
                    mAnonymousVoiceViewModel?.avoiceReject(id)
                }
            } else {
                mAnonymousVoiceViewModel?.hangUp()
            }
        }

        ll_quiet.onClickNew {
            //静音
            ll_quiet.isSelected = !ll_quiet.isSelected
            val volume = if (ll_quiet.isSelected) 0 else 100
            AgoraManager.mRtcEngine?.adjustRecordingSignalVolume(volume)
        }

        ll_voice_accept.onClickNew {
            //接受匿名语音
            mAnonymousVoiceViewModel?.inviteUserId?.let { id ->
                mAnonymousVoiceViewModel?.avoiceAccept(id)
            }

        }
        header_page.imageViewBack.onClickNew {
            val currentState = mAnonymousVoiceViewModel?.currentState?.value
            if (currentState == AnonymousVoiceViewModel.MATCH) {
                //调用取消匹配接口
                mAnonymousVoiceViewModel?.cancelMatch()
            }
            finish()
        }
        tv_match.onClickNew {
            checkPermissions()
        }
        tv_open_mine.onClickNew {
            //公开身份
            mAnonymousVoiceViewModel?.openIdentify()
        }

        tv_open_other.onClickNew {
            //揭秘身份
            mAnonymousVoiceViewModel?.checkBeans()
        }
        iv_follow.onClickNew {
            //关注按钮
            mAnonymousVoiceViewModel?.follow(mAnonymousVoiceViewModel?.targetUserId ?: return@onClickNew)
        }
    }


    /**
     * 公开和揭秘的时候显示用户信息
     */
    private fun showUserInfo(userInfo: UserInfoInRoom) {
        if (userInfo.userId == SessionUtils.getUserId()) {
            //显示本人信息
            showSexView(tv_sex_mine, userInfo.sex, "${userInfo.age}")
            tv_location_mine.text = userInfo.city
            tv_nickname_mine.text = userInfo.nickname
            view_left_header.loadImage("${userInfo.headPic}${BusiConstant.OSS_160}")
            showUserInfoAnimaiton(showMineInfoAnimatorSet, tv_open_mine, sdv_mine, con_userinfo_mine, view_left_header)
        } else {
            //显示对方信息
            mAnonymousVoiceViewModel?.targetUserId = userInfo.userId
            showSexView(tv_sex_other, userInfo.sex, "${userInfo.age}")
            tv_location_other.text = userInfo.city
            tv_nickname_other.text = userInfo.nickname
            view_right_header.loadImage("${userInfo.headPic}${BusiConstant.OSS_160}")
            if (userInfo.follow) {
                iv_follow.hide()
            } else {
                iv_follow.show()
            }
            showUserInfoAnimaiton(showOtherInfoAnimatorSet, tv_open_other, sdv_other, con_userinfo_other, con_header)
        }
    }


    /**
     * 显示性别相关的视图
     */
    private fun showSexView(sexView: TextView, sex: String, age: String) {
        var sexDrawable: Drawable? = null
        //性别
        when (sex) {
            Sex.MALE -> {
                sexView.backgroundResource = R.drawable.bg_shape_mkf_sex_male_anonymous
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                sexView.textColor = Color.parseColor("#58CEFF")
            }
            Sex.FEMALE -> {
                sexView.backgroundResource = R.drawable.bg_shape_mkf_sex_female_anonymous
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                sexView.textColor = Color.parseColor("#FF9BC5")
            }
            else -> sexDrawable = null
        }
        if (sexDrawable != null) {
            sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
            sexView.setCompoundDrawables(sexDrawable, null, null, null)
        } else {
            sexView.setCompoundDrawables(null, null, null, null)
        }
        sexView.text = age
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
                        if (mAnonymousVoiceViewModel?.basicData?.value == null) {
                            //未获取到数据
                            return@subscribe
                        }
                        if ((mAnonymousVoiceViewModel?.basicData?.value?.surplusTimes ?: 0 <= 0)) {
                            //没有可用次数
                            return@subscribe
                        }
                        if (!tv_match.isSelected) {
                            mAnonymousVoiceViewModel?.startMatch()
                            mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.MATCH
                        } else {
                            mAnonymousVoiceViewModel?.cancelMatch()
                            finish()
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
     * 开始匹配
     */
    private fun startMatch() {
        mDisposable = ObservableTake.interval(0, 1, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                tv_duration.text = "等待 ${TimeUtils.countDownTimeFormat1(mMatchStartTime + it)}"
                val remainder = it % 3
                val waitingContent = when (remainder) {
                    0L -> {
                        "请耐心等待哦."
                    }
                    1L -> {
                        "请耐心等待哦.."
                    }
                    else -> {
                        "请耐心等待哦..."
                    }
                }
                tv_waiting_content.text = waitingContent
            }, {})

        tv_match.isSelected = true
        tv_match.text = "取消匹配"
        tv_content.text = "欢鹊正在努力寻找最适合您的对象"
        tv_waiting_content.show()

        val disposableSweep = Observable.interval(0, 1500L, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                val remainder = it % 2
                if (remainder == 0L) {
                    startMatchAnimation(iv_animate_first, animatorFirstSet)
                } else {
                    startMatchAnimation(iv_animate_second, animatorSecondSet)
                }
            }, {})

        matchCompositeDisposable.add(disposableSweep)

        val disposableHeader = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(3)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it) {
                    0L -> {
                        startHeaderAnimation(sdv_left, leftHeaderAnimatorSet)
                    }
                    1L -> {
                        startHeaderAnimation(sdv_top, topHeaderAnimatorSet)
                    }
                    2L -> {
                        startHeaderAnimation(sdv_right, rightHeaderAnimatorSet)
                    }
                    else -> {
                    }
                }
            }, {})
        matchCompositeDisposable.add(disposableHeader)

    }

    /**
     * 停止匹配
     */
    private fun stopMatch() {
        mDisposable?.dispose()
        tv_match.isSelected = false
        val surplusTimes = mAnonymousVoiceViewModel?.basicData?.value?.surplusTimes ?: 0
        tv_duration.text = "剩余${surplusTimes}次"
        tv_match.text = "开始匹配"

        tv_match.isEnabled = surplusTimes > 0
        tv_content.text = "欢鹊会为你匹配最合拍的对象"
        tv_waiting_content.hide()
        animatorFirstSet.cancel()
        animatorSecondSet.cancel()
        leftHeaderAnimatorSet.cancel()
        topHeaderAnimatorSet.cancel()
        rightHeaderAnimatorSet.cancel()
        matchCompositeDisposable.clear()
    }

    /**
     * 开始匹配动画
     */
    private fun startMatchAnimation(view: View, set: AnimatorSet) {
        if (set.childAnimations.size > 0) {
            set.start()
            return
        }

        set.apply {
            val scaleXAnimate = ObjectAnimator.ofFloat(view, "scaleX", 0.4f, 1.57f)
            val scaleYAnimate = ObjectAnimator.ofFloat(view, "scaleY", 0.4f, 1.57f)
            val alphaAnimate = ObjectAnimator.ofFloat(view, "alpha", 1f, 1f, 1f, 1f, 0f)

            scaleXAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }

            scaleYAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }
            alphaAnimate.apply {
                duration = 2000
                interpolator = LinearInterpolator()
            }

            playTogether(scaleXAnimate, scaleYAnimate, alphaAnimate)
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    view.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    view.show()
                }

            })
        }
        set.start()
    }

    /**
     * 对方未加入频道 倒计时
     */
    private fun otherNoJoinCountDown() {
        mOtherNoJoinDisposable = Observable.timer(10, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .subscribe({
                mAnonymousVoiceViewModel?.hangUp()
            }, {})

    }

    /**
     * 开始头像缩放动画
     */
    private fun startHeaderAnimation(view: SimpleDraweeView, set: AnimatorSet) {
        if (set.childAnimations.size > 0) {
            set.start()
            return
        }
        set.apply {
            val scaleXAnimate = ObjectAnimator.ofFloat(view, "scaleX", 0f, 0.5f, 1f, 1f, 0.6f, 0f, 0f)
            val scaleYAnimate = ObjectAnimator.ofFloat(view, "scaleY", 0f, 0.5f, 1f, 1f, 0.6f, 0f, 0f)
            val alphaAnimate = ObjectAnimator.ofFloat(view, "alpha", 0.3f, 0.65f, 1f, 1f, 0.65f, 0.3f, 0.3f)

            scaleXAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }
            scaleYAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }
            alphaAnimate.apply {
                duration = 3000
                repeatCount = INFINITE
                repeatMode = RESTART
            }

            alphaAnimate.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                    val pics = mAnonymousVoiceViewModel?.basicData?.value?.headPics ?: return
                    val picSize = pics.size
                    if (picSize > 0) {
                        //获取随机头像显示
                        val index = java.util.Random().nextInt(picSize)
                        if (ForceUtils.isIndexNotOutOfBounds(index, pics)) {
                            view.loadImage("${pics[index]}${BusiConstant.OSS_120}", 60f, 60f)
                        }
                    }
                }

                override fun onAnimationEnd(animation: Animator?) {
                    view.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    view.show()
                }

            })

            playTogether(scaleXAnimate, scaleYAnimate, alphaAnimate)
        }
        set.start()
    }


    /**
     * 匹配成功
     */
    private fun showMatchSuccessView() {
        resetVoiceView()
        con_match.hide()
        header_page.imageViewBack.hide()
        if (voiceShowAnimatorSet.childAnimations.size <= 0) {
            //会话内容显示动画
            val voiceShowAnimator = ObjectAnimator.ofFloat(con_voice, "alpha", 0f, 1f)
            voiceShowAnimator.duration = 200
            voiceShowAnimator.interpolator = LinearInterpolator()
            //头像移动动画
            val mineHeaderTranslate = ObjectAnimator.ofFloat(sdv_mine, "translationX", 0f, -(dp2pxf(40) + dip(75) / 2))
            mineHeaderTranslate.duration = 200
            mineHeaderTranslate.interpolator = LinearInterpolator()
            //对方头像移动动画
            val otherHeaderTranslate = ObjectAnimator.ofFloat(sdv_other, "translationX", 0f, (dp2pxf(40) + dip(75) / 2))
            otherHeaderTranslate.duration = 200
            otherHeaderTranslate.interpolator = LinearInterpolator()

            voiceShowAnimatorSet.playTogether(voiceShowAnimator, mineHeaderTranslate, otherHeaderTranslate)
            voiceShowAnimatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    con_voice.show()
                }

            })

        }
        voiceShowAnimatorSet.start()

        val timerDisposable = Observable.timer(200, TimeUnit.MILLISECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                ImageUtils.loadGifImageLocal(sdv_animation, R.drawable.heat_bounce)
                showBtn()
            }, {})
        voiceCompositeDisposable.add(timerDisposable)
    }


    /**
     * 重置语音相关的视图
     */
    private fun resetVoiceView() {
        sdv_mine.show()
        sdv_mine.rotationY = 0f
        tv_open_mine.hide()
        tv_open_mine.rotationY = 0f

        con_userinfo_mine.alpha = 0f
        view_left_header.alpha = 0f

        sdv_other.show()
        sdv_other.rotationY = 0f
        tv_open_other.hide()
        tv_open_other.rotationY = 0f

        con_userinfo_other.alpha = 0f
        con_header.alpha = 0f

        ll_quiet.isSelected = false
        ll_close.isSelected = false
        ll_hands_free.isSelected = false
        ll_voice_accept.isSelected = false
    }

    /**
     * 开始匿名倒计时
     */
    private fun startVoiceCountDown() {
        ll_quiet.show()
        ll_close.show()
        ll_hands_free.show()
        ll_voice_accept.hide()
        val countDownDisposable = Observable.interval(0, 1, TimeUnit.SECONDS)
            .take(communicationTime + 1)
            .observeOn(AndroidSchedulers.mainThread())
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .subscribe({
                tv_reminder_time.text = "剩余时间 ${TimeUtils.countDownTimeFormat1(communicationTime - it)}"
            }, {}, {
                mAnonymousVoiceViewModel?.hangUp()
                leaveChannel()
            })


        voiceCompositeDisposable.add(countDownDisposable)

    }

    /**
     * 显示公开身份以及揭秘身份按钮
     */
    private fun showBtn() {
        if (showBtnAnimatorSet.childAnimations.size <= 0) {
            val mineAlphaAnimator = ObjectAnimator.ofFloat(tv_open_mine, "alpha", 0f, 1f)
            mineAlphaAnimator.duration = 200
            mineAlphaAnimator.interpolator = LinearInterpolator()

            val otherAlphaAnimator = ObjectAnimator.ofFloat(tv_open_other, "alpha", 0f, 1f)
            otherAlphaAnimator.duration = 200
            otherAlphaAnimator.interpolator = LinearInterpolator()

            showBtnAnimatorSet.playTogether(mineAlphaAnimator, otherAlphaAnimator)
        }

        showBtnAnimatorSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
                tv_open_mine.alpha = 0f
                tv_open_other.alpha = 0f
                tv_open_mine.show()
                tv_open_other.show()
            }

        })
        showBtnAnimatorSet.start()
    }

    /**
     * 显示等待接听状态
     */
    private fun showWaitAccept() {
        ll_close.show()
        ll_voice_accept.show()
    }

    /**
     * 显示用户信息动画
     */
    private fun showUserInfoAnimaiton(set: AnimatorSet, hideView: View, hideHeader: View, showView: View, showHeader: View) {
        if (set.childAnimations.size <= 0) {
            val hideAnimation = ObjectAnimator.ofFloat(hideView, "rotationY", 0f, 90f)
            hideAnimation.duration = 250
            hideAnimation.interpolator = LinearInterpolator()
            hideAnimation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    hideView.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })

            val hideHeaderAnimation = ObjectAnimator.ofFloat(hideHeader, "rotationY", 0f, 90f)
            hideHeaderAnimation.duration = 250
            hideHeaderAnimation.interpolator = LinearInterpolator()
            hideHeaderAnimation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    hideHeader.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                }
            })

            val showAnimation = ObjectAnimator.ofFloat(showView, "rotationY", -90f, 0f)
            showAnimation.duration = 250
            showAnimation.interpolator = LinearInterpolator()
            showAnimation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    showView.alpha = 1f
                }

            })

            val showHeaderAnimation = ObjectAnimator.ofFloat(showHeader, "rotationY", -90f, 0f)
            showHeaderAnimation.duration = 250
            showHeaderAnimation.interpolator = LinearInterpolator()
            showHeaderAnimation.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    showHeader.alpha = 1f
                }

            })

            set.play(hideAnimation).with(hideHeaderAnimation).before(showAnimation).before(showHeaderAnimation)
        }
        set.start()
    }

    override fun onStop() {
        super.onStop()
        stopMatch()
        matchCompositeDisposable.clear()
        voiceCompositeDisposable.clear()
        VoiceManager.stopAllVoice()
        val currentState = mAnonymousVoiceViewModel?.currentState?.value
        if (currentState == AnonymousVoiceViewModel.MATCH) {
            //调用取消匹配接口
            mAnonymousVoiceViewModel?.cancelMatch()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageProcessor.clearProcessors(false)
    }

    override fun onRemoteAudioStats(stats: IRtcEngineEventHandler.RemoteAudioStats?) {
    }

    override fun onRtcStats(stats: IRtcEngineEventHandler.RtcStats?) {
    }

    override fun onUserJoined(uid: Int, elapsed: Int) {
        if (uid.toLong() != SessionUtils.getUserId()) {
            mOtherNoJoinDisposable?.dispose()
        }
    }

    override fun onLocalVideoStats(stats: IRtcEngineEventHandler.LocalVideoStats?) {
    }

    override fun onAudioRouteChanged(routing: Int) {
        ll_hands_free.isEnabled = !(routing == Constants.AUDIO_ROUTE_HEADSET || routing == Constants.AUDIO_ROUTE_HEADSETBLUETOOTH)
    }

    /**
     * 网络连接中断，且 SDK 无法在 10 秒内连接服务器回调。
     */
    override fun onConnectionLost() {
        mAnonymousVoiceViewModel?.currentState?.postValue(AnonymousVoiceViewModel.WAIT)
    }

    override fun onTokenPrivilegeWillExpire(token: String?) {
    }

    override fun onUserOffline(uid: Int, reason: Int) {
        logger.info("AGORA Message 用户掉线")
        mAnonymousVoiceViewModel?.hangUp()
//        mAnonymousVoiceViewModel?.currentState?.postValue(AnonymousVoiceViewModel.WAIT)
    }

    override fun onRemoteVideoStats(stats: IRtcEngineEventHandler.RemoteVideoStats?) {
    }

    override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
    }

    override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
    }

    override fun onLastmileProbeResult(result: IRtcEngineEventHandler.LastmileProbeResult?) {
    }

    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
    }

    override fun onLastmileQuality(quality: Int) {
    }

    override fun onRequestToken() {
    }

    override fun onFirstRemoteVideoDecoded(uid: Int, width: Int, height: Int, elapsed: Int) {
    }

    override fun onLeaveChannel(stats: IRtcEngineEventHandler.RtcStats?) {
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        if (reason == Constants.CONNECTION_CHANGED_BANNED_BY_SERVER) {
            mAnonymousVoiceViewModel?.hangUp()
//            mAnonymousVoiceViewModel?.currentState?.value = AnonymousVoiceViewModel.WAIT
        }
    }

    override fun onError(err: Int) {
    }

    // Tutorial Step 2
    private fun joinChannel() {
        var accessToken = mAnonymousVoiceViewModel?.agoraToken ?: ""
        val channelId = mAnonymousVoiceViewModel?.channelId ?: return
        if (accessToken.isEmpty()) {
            return
        }
        //设置为主播身份
        AgoraManager.mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        AgoraManager.mRtcEngine?.enableAudio()
        AgoraManager.mRtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
        //默认开启声音
        AgoraManager.mRtcEngine?.adjustRecordingSignalVolume(100)
        //默认听筒
        // Allows a user to join a channel.
        val result = AgoraManager.mRtcEngine?.joinChannel(
            accessToken,
            channelId,
            null,
            SessionUtils.getUserId().toInt()
        )
        if (result != 0) {
            //joinchannel失败
            mAnonymousVoiceViewModel?.hangUp()
        }
//        logger.info("$TAG joinResult = $result")
    }

    private fun leaveChannel() {
        AgoraManager.mRtcEngine?.leaveChannel()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        endClose = false
        judgeType(intent)
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
        SharedPreferencesUtils.commitBoolean(SPParamKey.VOICE_ON_LINE, false)
        leaveChannel()
        mDisposable?.dispose()
        VoiceManager.stopAllVoice()
    }

    override fun onBackPressed() {
        val currentState = mAnonymousVoiceViewModel?.currentState?.value
        if (currentState == AnonymousVoiceViewModel.MATCH) {
            //调用取消匹配接口
            mAnonymousVoiceViewModel?.cancelMatch()
        }
        if (currentState == AnonymousVoiceViewModel.VOICE || currentState == AnonymousVoiceViewModel.WAIT_ACCEPT) {
            //通话中，或者等待接听状态   禁用返回键
            return
        }
        super.onBackPressed()
    }

}