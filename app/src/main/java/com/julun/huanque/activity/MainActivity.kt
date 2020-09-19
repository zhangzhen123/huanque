package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.julun.huanque.R
import com.julun.huanque.agora.activity.AnonymousVoiceActivity
import com.julun.huanque.app.update.AppChecker
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.LoadingDialog
import com.julun.huanque.common.basic.VoidResult
import com.julun.huanque.common.bean.beans.AnonyVoiceInviteBean
import com.julun.huanque.common.bean.beans.IntimateBean
import com.julun.huanque.common.bean.beans.NetCallReceiveBean
import com.julun.huanque.common.bean.beans.OperatorMessageBean
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.bean.forms.SaveLocationForm
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.core.ui.main.bird.LeYuanBirdActivity
import com.julun.huanque.core.ui.main.home.HomeFragment
import com.julun.huanque.message.fragment.MessageFragment
import com.julun.huanque.message.viewmodel.MessageViewModel
import com.julun.huanque.support.LoginManager
import com.julun.huanque.core.ui.main.bird.LeYuanFragment
import com.julun.huanque.fragment.PersonalInformationProtectionFragment
import com.julun.huanque.fragment.UpdateInfoFragment
import com.julun.huanque.ui.main.MineFragment
import com.julun.huanque.viewmodel.FillInformationViewModel
import com.julun.huanque.viewmodel.MainViewModel
import com.julun.maplib.LocationService
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.rong.imlib.RongIMClient
import kotlinx.android.synthetic.main.act_fill_information.*
import kotlinx.android.synthetic.main.main_activity.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity
import java.util.concurrent.TimeUnit
import kotlin.math.abs


@Route(path = ARouterConstant.MAIN_ACTIVITY)
class MainActivity : BaseActivity() {

    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mLeYuanFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }

    private val mMessageFragment: MessageFragment by lazy { MessageFragment.newInstance() }
    private val mMineFragment: MineFragment by lazy { MineFragment.newInstance() }

    //隐私弹窗
    private var mProtectionFragment: PersonalInformationProtectionFragment? = null

    //    private val mMineFragment: Fragment by lazy { RNPageFragment.start("PH") }


    private val mMainViewModel: MainViewModel by viewModels()

    private val mMessageViewModel: MessageViewModel by viewModels()

    private val mFillInformationViewModel: FillInformationViewModel by viewModels()

    private var firstTime = 0L

    //封装百度地图相关的Service
    private lateinit var mLocationService: LocationService

    private var mUpdateInfoFragment: UpdateInfoFragment? = null

    //百度地图监听的Listener
    private var mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            logger.info("location error=${location?.locTypeDescription}")
            if (null != location && location.locType != BDLocation.TypeServerError) {
                logger.info("location=${location.addrStr}")
                //获得一次结果，就结束定位
                stopLocation()
                mMainViewModel.saveLocation(
                    SaveLocationForm(
                        "${location.latitude}",
                        "${location.longitude}",
                        location.city,
                        location.province,
                        location.district
                    )
                )
            }
        }
    }

    override fun isRegisterEventBus(): Boolean = true

    override fun getLayoutId() = R.layout.main_activity

    override fun onCreate(savedInstanceState: Bundle?) {
        //防止重建的缓存自动恢复
        super.onCreate(null)
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        if (SessionUtils.getIsRegUser() && SessionUtils.getSessionId().isNotEmpty()) {
            AppChecker.startCheck(true)
//            UserHeartManager.startOnline()
        } else {
            ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
        }
        intent?.let {
            judgeUpdateInfoFragment(it)
        }

        CommonInit.getInstance().setMainActivity(this)
        logger.info("DXC  userID = ${SessionUtils.getUserId()}，header = ${SessionUtils.getHeaderPic()}")
        initViewModel()
        val targetIndex = intent?.getIntExtra(IntentParamKey.TARGET_INDEX.name, 0) ?: 0
        mMainViewModel.indexData.value = targetIndex


        mLocationService = LocationService(this.applicationContext)
        mLocationService.registerListener(mLocationListener)
        mLocationService.setLocationOption(mLocationService.defaultLocationClientOption.apply {
            //                            this.setScanSpan(0)
//                            this.setIgnoreKillProcess(false)
        })

        registerMessage()
        //查询未读数
        if (RongIMClient.ConnectionStatusListener.ConnectionStatus.CONNECTED == RongIMClient.getInstance().currentConnectionStatus) {
            //融云已经连接
            mMainViewModel.getUnreadCount()
        }
        //延迟获取定位权限
        Observable.timer(3, TimeUnit.SECONDS)
            .bindUntilEvent(this, ActivityEvent.DESTROY)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ checkPermission() }, { it.printStackTrace() })
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        mLocationService.stop()
        mLocationService.unregisterListener(mLocationListener)
    }

    /**
     * 判断是否显示更新用户数据弹窗
     */
    private fun judgeUpdateInfoFragment(intent: Intent) {
//        mProtectionFragment = mProtectionFragment ?: PersonalInformationProtectionFragment.newInstance(PersonalInformationProtectionFragment.MainActivity)
//        mProtectionFragment?.show(supportFragmentManager, "PersonalInformationProtectionFragment")
        val birthday = intent.getStringExtra(ParamConstant.Birthday)
        if (birthday?.isNotEmpty() == true) {
            mUpdateInfoFragment =
                mUpdateInfoFragment ?: UpdateInfoFragment.newInstance(birthday)
            mUpdateInfoFragment?.show(supportFragmentManager, "UpdateInfoFragment")
        }

    }


    /**
     * 检查定位权限
     */
    private fun checkPermission() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .subscribe { permission ->
                //不管有没有给权限 都不影响百度定位 只不过不给权限会不太准确
                mLocationService.start()
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        logger.info("获取定位被拒绝")
                    else -> {
                        logger.info("获取定位被永久拒绝")
                    }
                }

            }
    }


    override fun onStop() {
        super.onStop()
        stopLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        UserHeartManager.stopBeat()
        RongIMClient.getInstance().disconnect()
    }


    private fun initViewModel() {
        mMainViewModel.indexData.observe(this, Observer {
            if (it != null) {
                goToTab(it)
            }
        })
        mMainViewModel.unreadMsgCount.observe(this, Observer {
            if (it != null) {
                mMessageViewModel.unreadMsgCount.value = it
                tv_unread_count.text = if (it <= 99) {
                    "$it"
                } else {
                    "99+"
                }
                showUnreadCount()
            }
        })

        mMessageViewModel.queryUnreadCountFlag.observe(this, Observer {
            if (it == true) {
                mMainViewModel.getUnreadCount()
                mMessageViewModel.queryUnreadCountFlag.value = false
            }
        })

        mFillInformationViewModel.openPicFlag.observe(this, Observer {
            if (it == true) {
                //打开相册
                checkPermissions()
            }
        })
        mFillInformationViewModel.headerPicData.observe(this, Observer {
            if (it != null) {
                mLoadingDialog.dismiss()
            }
        })

    }

    override fun initEvents(rootView: View) {
        view_make_friends.onClickNew {
            //交友
            if (getCurrentFragment() != mHomeFragment) {
                tabIconAnimation(MainPageIndexConst.MAIN_FRAGMENT_INDEX)
            } else {
                mHomeFragment.scrollToTop()
            }
            showFragmentNew(MainPageIndexConst.MAIN_FRAGMENT_INDEX)
        }
        view_leyuan.onClickNew {
            //乐园
//            if (getCurrentFragment() != mLeYuanFragment) {
//                tabIconAnimation(MainPageIndexConst.LEYUAN_FRAGMENT_INDEX)
//            }
//            showFragmentNew(MainPageIndexConst.LEYUAN_FRAGMENT_INDEX)
            startActivity<LeYuanBirdActivity>()

        }
        view_message.onClickNew {
            //消息
            if (getCurrentFragment() != mMessageFragment) {
                tabIconAnimation(MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
            }
            showFragmentNew(MainPageIndexConst.MESSAGE_FRAGMENT_INDEX)
        }
        view_mine.onClickNew {
            //我的
            if (getCurrentFragment() != mMineFragment) {
                tabIconAnimation(MainPageIndexConst.MINE_FRAGMENT_INDEX)
            }
            showFragmentNew(MainPageIndexConst.MINE_FRAGMENT_INDEX)
        }
    }

    /**
     * 切换到指定[index]tab页
     */
    private fun goToTab(index: Int) {
        when (index) {
            MainPageIndexConst.MAIN_FRAGMENT_INDEX -> {
                view_make_friends.performClick()
            }
            MainPageIndexConst.LEYUAN_FRAGMENT_INDEX -> {
                view_leyuan.performClick()
            }
            MainPageIndexConst.MESSAGE_FRAGMENT_INDEX -> {
                view_message.performClick()
            }
            MainPageIndexConst.MINE_FRAGMENT_INDEX -> {
                view_mine.performClick()
            }
        }
    }

    /**
     * tab上面的imageview添加动画
     */
    private fun tabIconAnimation(index: Int) {
        val list = arrayOf(lottie_make_friends, lottie_leyuan, lottie_message, lottie_mine)
        val textList = arrayOf(item_make_friends, item_leyuan, item_message, item_mine)
        list.forEachIndexed { position, lottieAnimationView ->
            if (index == position) {
                if (!lottieAnimationView.isAnimating) {
                    lottieAnimationView.setSpeed(1f)
                    lottieAnimationView.playAnimation()
                }
                textList[position].isSelected = true
            } else {
                lottieAnimationView.cancelAnimation()
                lottieAnimationView.progress = 0f
                textList[position].isSelected = false
            }
        }
        showUnreadCount()
    }


    /**
     * 切换fragment
     */
    private fun showFragmentNew(index: Int): Boolean {
        //切换我的收藏和个人中心时1,2前检测登录状态
        val oldFragment: Fragment? = getCurrentFragment()
        val newFragment: Fragment = getFragmentByIndex(index) ?: return false
        // 新的framgnet已经显示了（比如loginFragment，未登录时从个人中心切换到关注）
        val transaction = supportFragmentManager.beginTransaction()
        if (oldFragment != null) {
            transaction.hide(oldFragment)
        }
        if (!newFragment.isAdded) {
            transaction.add(R.id.container, newFragment, newFragment.javaClass.name)
        }
        transaction.show(newFragment).commitAllowingStateLoss()
        //使用此方式在主线程中立即执行事务队列所有事务，同步当前的状态,确保来回快速切换的时候事务不会堆积在队列中异步执行， 避免卡顿问题
        supportFragmentManager.executePendingTransactions()
        return true
    }

    /**
     * 根据index获取fragment
     */
    private fun getFragmentByIndex(index: Int): Fragment? {
        return when (index) {
            MainPageIndexConst.MAIN_FRAGMENT_INDEX -> mHomeFragment
            MainPageIndexConst.LEYUAN_FRAGMENT_INDEX -> mLeYuanFragment
            MainPageIndexConst.MESSAGE_FRAGMENT_INDEX -> mMessageFragment
            MainPageIndexConst.MINE_FRAGMENT_INDEX -> mMineFragment
            else -> {
                null
            }
        }
    }

    /**
     * 获取当前的fragment
     */
    private fun getCurrentFragment(): Fragment? {
        if (mHomeFragment.isVisible) {
            return mHomeFragment
        }
        if (mLeYuanFragment.isVisible) {
            return mLeYuanFragment
        }
        if (mMessageFragment.isVisible) {
            return mMessageFragment
        }
        if (mMineFragment.isVisible) {
            return mMineFragment
        }
        return null
    }

    /**
     * 显示未读数
     */
    private fun showUnreadCount() {
        val unreadCount = mMainViewModel.unreadMsgCount.value ?: 0
        if (!item_message.isSelected && unreadCount > 0) {
            //未选中消息模块，同时未读数大于0(显示)
            tv_unread_count.show()
        } else {
            tv_unread_count.hide()
        }

    }


    override fun onBackPressed() {
        exit()
    }

    /**
     * 退出应用
     */
    private fun exit() {
        if (!mHomeFragment.isVisible) {
            mMainViewModel.indexData.value = 0
            return
        }
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime < 2000) {
            //两秒之内点击了两次返回键  退出程序
            finish()
            ActivitiesManager.INSTANCE.finishApp()
        } else {
            ToastUtils.show("再按一次退出程序")
            firstTime = secondTime
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val targetIndex = intent?.getIntExtra(IntentParamKey.TARGET_INDEX.name, 0) ?: 0
        mMainViewModel.indexData.value = targetIndex
        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
            intent?.let {
                judgeUpdateInfoFragment(it)
            }
        } else {
            SessionUtils.clearSession()
            finish()
        }
    }

    private fun registerMessage() {
        MessageProcessor.clearProcessors(true)
        MessageProcessor.registerEventProcessor(object : MessageProcessor.NetCallReceiveProcessor {
            override fun process(data: NetCallReceiveBean) {
                val onLine = SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)
                if (onLine) {
                    //正在通话中
                    mMainViewModel.busy(data.callId)
                    mMainViewModel.insertMessage(data.callUserId)
                } else {
                    mMainViewModel.getVoiceCallInfo(data.callId)
                }

            }
        })

        //亲密度变化消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.IntimateChangeProcessor {
            override fun process(data: IntimateBean) {
                //发送消息，私信列表接收
                EventBus.getDefault().post(data)
                val userMap = hashMapOf<String, Any>()
                data.userIds.forEach { userId ->
                    if (userId != SessionUtils.getUserId()) {
                        //获取到对方的ID
                        userMap.put("friendId", userId)
                        userMap.put("intimateLevel", data.intimateLevel)
                        return@forEach
                    }
                }
                val event = SendRNEvent(RNMessageConst.IntimateFriendChange, userMap)
                //发送消息，通知RN
                EventBus.getDefault().post(event)
            }
        })

        //邀请匿名语音消息
        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.AnonyVoiceInviteProcessor {
            override fun process(data: AnonyVoiceInviteBean) {
                if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                    return
                }
                if (abs(System.currentTimeMillis() - data.inviteTime) > 30 * 1000) {
                    //邀请消息时间，与本地时间  相差超过30秒，直接忽略
                    return
                }
                val intent = Intent(this@MainActivity, AnonymousVoiceActivity::class.java)
                intent.putExtra(ParamConstant.TYPE, ConmmunicationUserType.CALLED)
                intent.putExtra(ParamConstant.InviteUserId, data.inviteUserId)
                if (ForceUtils.activityMatch(intent)) {
                    startActivity(intent)
                }
            }
        })

        MessageProcessor.registerEventProcessor(object :
            MessageProcessor.RefreshUserSettingProcessor {
            override fun process(data: VoidResult) {
                mMainViewModel.getSetting()
            }
        })
        //封禁账户消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.BanUserProcessor {
            override fun process(data: OperatorMessageBean) {
                ToastUtils.show("您已被封禁账号")
                FloatingManager.hideFloatingView()
                EventBus.getDefault().post(BannedAndClosePlayer())
                EventBus.getDefault().post(LoginOutEvent())
            }
        })
        //直播封禁（用户不允许进入任何直播间）
        MessageProcessor.registerEventProcessor(object : MessageProcessor.BanUserLivingProcessor {
            override fun process(data: OperatorMessageBean) {
                ToastUtils.show("您已无法访问直播功能")
                EventBus.getDefault().post(BannedAndClosePlayer())
                FloatingManager.hideFloatingView()
            }
        })
        //踢人消息
        MessageProcessor.registerEventProcessor(object : MessageProcessor.KickUserProcessor {
            override fun process(data: OperatorMessageBean) {
                EventBus.getDefault().post(BannedAndClosePlayer(data.programId))
                val programId = SharedPreferencesUtils.getLong(SPParamKey.PROGRAM_ID_IN_FLOATING, 0)
                if (programId == data.programId) {
                    //如果悬浮窗正在播放的是被踢出的直播间就处理
                    FloatingManager.hideFloatingView()
                }
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            goToTab(MainPageIndexConst.MAIN_FRAGMENT_INDEX)
            //重新去定位地址
            mLocationService.registerListener(mLocationListener)
            mLocationService.start()
        } else {
            mMainViewModel.unreadMsgCount.value = 0
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateMessageReceive(bean: EventMessageBean) {
        //接收到私聊消息
        mMainViewModel.getUnreadCount()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun unreadCount(bean: QueryUnreadCountEvent) {
        if (!bean.player) {
            EventBus.getDefault()
                .postSticky(UnreadCountEvent(mMainViewModel.unreadMsgCount.value ?: 0, false))
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun connectSuccess(event: RongConnectEvent) {
        if (RongCloudManager.RONG_CONNECTED == event.state) {
            //融云连接成功，查询未读数
            //查询免打扰列表
            mMainViewModel.getUnreadCount()
            mMainViewModel.refreshMessage()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun loginOut(event: LoginOutEvent) {
        //登录通知
        LoginManager.doLoginOut({
            //退出登录成功
            ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun hideFloating(event: HideFloatingEvent) {
        FloatingManager.hideFloatingView()
    }


    override fun finish() {
        FloatingManager.hideFloatingView()
        super.finish()
    }

    private fun checkPermissions() {
        val rxPermissions = RxPermissions(this)
        rxPermissions
            .requestEachCombined(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        goToPictureSelectPager()
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

    private val mLoadingDialog: LoadingDialog by lazy { LoadingDialog(this) }

    /**
     *
     */
    private fun goToPictureSelectPager() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
            .theme(R.style.picture_me_style_single)// 主题样式设置 具体参考 values/styles   用法：R.style.picture.white.style
            .minSelectNum(1)// 最小选择数量
            .imageSpanCount(4)// 每行显示个数
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(false)// 是否可预览图片
            .isCamera(true)// 是否显示拍照按钮
            .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
            .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
            //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径
            .enableCrop(true)// 是否裁剪
            .compress(true)// 是否压缩
            .synOrAsy(true)//同步true或异步false 压缩 默认同步
            //.compressSavePath(getPath())//压缩图片保存地址
            .glideOverride(120, 120)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度
            .isGif(false)// 是否显示gif图片
//                    .selectionMedia(selectList)// 是否传入已选图片
            .previewEggs(true)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
            //.cropCompressQuality(90)// 裁剪压缩质量 默认100
            .minimumCompressSize(100)// 小于100kb的图片不压缩
//            .cropWH(200, 200)// 裁剪宽高比，设置如果大于图片本身宽高则无效
            .withAspectRatio(1, 1)// 裁剪比例 如16:9 3:2 3:4 1:1 可自定义
            .hideBottomControls(true)// 是否显示uCrop工具栏，默认不显示
            .freeStyleCropEnabled(true)// 裁剪框是否可拖拽
            .isDragFrame(false)
//            .circleDimmedLayer(true)// 是否圆形裁剪
//            .showCropFrame(false)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
//            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false
//            .rotateEnabled(false) // 裁剪是否可旋转图片
            .scaleEnabled(true)// 裁剪是否可放大缩小图片
            .forResult(PictureConfig.CHOOSE_REQUEST)

        //结果回调onActivityResult code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == PictureConfig.CHOOSE_REQUEST) {
                logger.info("收到图片")
                val selectList = PictureSelector.obtainMultipleResult(data)
                for (media in selectList) {
                    Log.i("图片-----》", media.path)
                }
                if (selectList.size > 0) {
                    val media = selectList[0]
                    val path: String?
                    path = if (media.isCut && !media.isCompressed) {
                        // 裁剪过
                        media.cutPath
                    } else if (media.isCompressed || media.isCut && media.isCompressed) {
                        // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                        media.compressPath
                    } else {
                        media.path
                    }
                    logger.info("收到图片:$path")
                    if (!mLoadingDialog.isShowing) {
                        mLoadingDialog.showDialog()
                    }

                    mFillInformationViewModel.uploadHead(path)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            logger.info("图片返回出错了")
        }
    }

}