package com.julun.huanque.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.julun.huanque.R
import com.julun.huanque.app.update.AppChecker
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.NetCallReceiveBean
import com.julun.huanque.common.bean.events.*
import com.julun.huanque.common.bean.forms.SaveLocationForm
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.SPParamKey
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.manager.UserHeartManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.SharedPreferencesUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.core.manager.FloatingManager
import com.julun.huanque.core.ui.main.home.HomeFragment
import com.julun.huanque.message.fragment.MessageFragment
import com.julun.huanque.message.viewmodel.MessageViewModel
import com.julun.huanque.support.LoginManager
import com.julun.huanque.ui.main.LeYuanFragment
import com.julun.huanque.ui.main.MineFragment
import com.julun.huanque.viewmodel.MainViewModel
import com.julun.maplib.LocationService
import io.rong.imlib.RongIMClient
import kotlinx.android.synthetic.main.fragment_mine.*
import kotlinx.android.synthetic.main.main_activity.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = ARouterConstant.MAIN_ACTIVITY)
class MainActivity : BaseActivity() {

    private val mHomeFragment: HomeFragment by lazy { HomeFragment.newInstance() }
    private val mLeYuanFragment: LeYuanFragment by lazy { LeYuanFragment.newInstance() }

    private val mMessageFragment: MessageFragment by lazy { MessageFragment.newInstance() }
    private val mMineFragment: MineFragment by lazy { MineFragment.newInstance() }

    //    private val mMineFragment: Fragment by lazy { RNPageFragment.start("PH") }

    companion object {
        private const val MAIN_FRAGMENT_INDEX = 0
        private const val LEYUAN_FRAGMENT_INDEX = 1
        private const val MESSAGE_FRAGMENT_INDEX = 2
        private const val MINE_FRAGMENT_INDEX = 3
    }


    private val mMainViewModel: MainViewModel by viewModels()

    private val mMessageViewModel: MessageViewModel by viewModels()

    private var firstTime = 0L

    //封装百度地图相关的Service
    private lateinit var mLocationService: LocationService

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
            UserHeartManager.startOnline()
        } else {
            ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
        }

        CommonInit.getInstance().setMainActivity(this)
        logger.info("DXC  userID = ${SessionUtils.getUserId()}，header = ${SessionUtils.getHeaderPic()}")
        initViewModel()
        mMainViewModel.indexData.value = 0


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
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        mLocationService.stop()
        mLocationService.unregisterListener(mLocationListener)
    }

    override fun onStart() {
        super.onStart()
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

    }

    override fun initEvents(rootView: View) {
        view_make_friends.onClickNew {
            //交友
            if (getCurrentFragment() != mHomeFragment) {
                tabIconAnimation(MAIN_FRAGMENT_INDEX)
            }else{
                mHomeFragment.scrollToTop()
            }
            showFragmentNew(MAIN_FRAGMENT_INDEX)
        }
        view_leyuan.onClickNew {
            //乐园
            if (getCurrentFragment() != mLeYuanFragment) {
                tabIconAnimation(LEYUAN_FRAGMENT_INDEX)
            }
            showFragmentNew(LEYUAN_FRAGMENT_INDEX)
        }
        view_message.onClickNew {
            //消息
            if (getCurrentFragment() != mMessageFragment) {
                tabIconAnimation(MESSAGE_FRAGMENT_INDEX)
            }
            showFragmentNew(MESSAGE_FRAGMENT_INDEX)
        }
        view_mine.onClickNew {
            //我的
            if (getCurrentFragment() != mMineFragment) {
                tabIconAnimation(MINE_FRAGMENT_INDEX)
            }
            showFragmentNew(MINE_FRAGMENT_INDEX)
        }
    }

    /**
     * 切换到指定[index]tab页
     */
    private fun goToTab(index: Int) {
        when (index) {
            MAIN_FRAGMENT_INDEX -> {
                view_make_friends.performClick()
            }
            LEYUAN_FRAGMENT_INDEX -> {
                view_leyuan.performClick()
            }
            MESSAGE_FRAGMENT_INDEX -> {
                view_message.performClick()
            }
            MINE_FRAGMENT_INDEX -> {
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
            MAIN_FRAGMENT_INDEX -> mHomeFragment
            LEYUAN_FRAGMENT_INDEX -> mLeYuanFragment
            MESSAGE_FRAGMENT_INDEX -> mMessageFragment
            MINE_FRAGMENT_INDEX -> mMineFragment
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
            ActivitiesManager.finishApp()
        } else {
            ToastUtils.show("再按一次退出程序")
            firstTime = secondTime
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (SessionUtils.getIsRegUser() && SessionUtils.getRegComplete()) {
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

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLoginCode(event: LoginEvent) {
        logger.info("登录事件:${event.result}")
        if (event.result) {
            goToTab(MAIN_FRAGMENT_INDEX)
            //重新去定位地址
            mLocationService.registerListener(mLocationListener)
            mLocationService.start()
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun privateMessageReceive(bean: EventMessageBean) {
        //接收到私聊消息
        mMainViewModel.getUnreadCount()
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
        LoginManager.doLoginOut {
            if (it) {
                //退出登录成功
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun finish() {
        FloatingManager.hideFloatingView()
        super.finish()
    }

}