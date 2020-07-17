package com.julun.huanque.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.julun.huanque.R
import com.julun.huanque.app.HuanQueApp
import com.julun.huanque.app.update.AppChecker
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.bean.beans.NetCallReceiveBean
import com.julun.huanque.common.bean.events.LoginEvent
import com.julun.huanque.common.bean.events.WeiXinCodeEvent
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.ConmmunicationUserType
import com.julun.huanque.common.constant.ParamKey
import com.julun.huanque.common.init.CommonInit
import com.julun.huanque.common.manager.ActivitiesManager
import com.julun.huanque.common.manager.RongCloudManager
import com.julun.huanque.common.message_dispatch.MessageProcessor
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.ui.main.home.HomeFragment
import com.julun.huanque.message.fragment.MessageFragment
import com.julun.huanque.ui.main.*
import com.julun.huanque.viewmodel.MainViewModel
import com.julun.maplib.LocationService
import com.julun.rnlib.RNPageFragment
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = ARouterConstant.MAIN_ACTIVITY)
class MainActivity : BaseActivity() {

    private val mHomeFragment = HomeFragment.newInstance()
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


    private var mMainViewModel: MainViewModel? = null

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
                //todo
//                viewModel.saveLocation("${location.latitude}", "${location.longitude}", location.province, location.city, location.district)
            }
        }
    }

    override fun isRegisterEventBus(): Boolean=true

    override fun getLayoutId() = R.layout.main_activity

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        if(SessionUtils.getIsRegUser()&&SessionUtils.getSessionId().isNotEmpty()){
            AppChecker.startCheck(true)
        }else{
            ARouter.getInstance().build(ARouterConstant.LOGIN_ACTIVITY).navigation()
        }

        CommonInit.getInstance().setMainActivity(this)
        logger.info("DXC  userID = ${SessionUtils.getUserId()}，header = ${SessionUtils.getHeaderPic()}")
        setContentView(R.layout.main_activity)
        initViewModel()
        mMainViewModel?.indexData?.value = 0


        mLocationService = LocationService(this.applicationContext)
        mLocationService.registerListener(mLocationListener)
        mLocationService.setLocationOption(mLocationService?.defaultLocationClientOption.apply {
            //                            this?.setScanSpan(0)
//                            this?.setIgnoreKillProcess(false)
        })

        registerMessage()

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
        mLocationService.start()
    }

    override fun onStop() {
        super.onStop()
        stopLocation()
    }

    private fun initViewModel() {
        mMainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        mMainViewModel?.indexData?.observe(this, Observer {
            if (it != null) {
                goToTab(it)
            }
        })
    }

    override fun initEvents(rootView: View) {
        view_make_friends.onClickNew {
            //交友
            if (getCurrentFragment() != mHomeFragment) {
                tabIconAnimation(MAIN_FRAGMENT_INDEX)
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
        lifecycleScope.launch {
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
        }
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
            transaction.add(R.id.container, newFragment, "$index")
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

    override fun onBackPressed() {
        exit()
    }

    /**
     * 退出应用
     */
    private fun exit() {
        if (!mHomeFragment.isVisible) {
            mMainViewModel?.indexData?.value = 0
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
                mMainViewModel?.getVoiceCallInfo(data.callId)
            }
        })

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLoginCode(event: LoginEvent){
        logger.info("登录事件:${event.result}")
        if(event.result){

        }

    }

}