package com.julun.huanque.core.ui.homepage

import android.Manifest
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.AppBarLayout
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.CommonDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.bean.events.ImagePositionEvent
import com.julun.huanque.common.bean.events.PicChangeEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.common.widgets.layoutmanager.AutoCenterLayoutManager
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageAdapter
import com.julun.huanque.core.adapter.HomePagePicListAdapter
import com.julun.huanque.core.ui.record_voice.VoiceSignActivity
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import kotlinx.android.synthetic.main.act_home_page.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs

/**
 *@创建者   dong
 *@创建时间 2020/9/29 10:51
 *@描述 他人主页
 */
@Route(path = ARouterConstant.HOME_PAGE_ACTIVITY)
class HomePageActivity : BaseActivity() {
    companion object {
        fun newInstance(act: Activity, userId: Long, showPic: String = "") {
            val intent = Intent(act, HomePageActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.UserId, userId)
                intent.putExtra(ParamConstant.ShowPic, showPic)
                act.startActivity(intent)
            }
        }
    }

    private val mHomePageViewModel: HomePageViewModel by viewModels()

    private val huanQueViewModel = HuanViewModelManager.huanQueViewModel

    private val mPicAdapter = HomePagePicListAdapter()


    private lateinit var mCommonNavigator: CommonNavigator

    //更多操作弹窗
    private var mHomePageActionFragment: HomePageActionFragment? = null

    //主页评论弹窗
    private var mHomePageEvaluateFragment: HomePageEvaluateFragment? = null

    private var mPagerAdapter: HomePageAdapter? = null

    //顶部开始渐变的位置
    private var mStartChange = 0

    //顶部多长时间变化完成
    private val mChangeDistance = 100

    //亲密知己弹窗
    private val mIntimacyFragment: IntimacyFragment by lazy { IntimacyFragment() }

    //座驾详情弹窗
    private val mCarDetailFragment: CarDetailFragment by lazy { CarDetailFragment() }

    private val mTabTitles = arrayListOf<String>("资料", "动态")

    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(this) }

    //实人认证邀请弹窗
    private var mRealPeopleAttentionFragment: RealPeopleAttentionFragment? = null

    private var mShowPic: String = ""

    override fun getLayoutId() = R.layout.act_home_page

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        intent?.let { tent ->
            resetView(tent)
        }
        val statusHeight = StatusBarUtil.getStatusBarHeight(this)
        mStartChange = ScreenUtils.getScreenWidth() * 308 / 375 - dp2px(44) - statusHeight

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            /**
//             * 1、设置相同的TransitionName
//             */
//            ViewCompat.setTransitionName(bga_banner, "Image${mHomePageViewModel.targetUserId}")
////            ViewCompat.setTransitionName(tv_nickname, "TextView${mHomePageViewModel.targetUserId}")
//            /**
//             * 2、设置WindowTransition,除指定的ShareElement外，其它所有View都会执行这个Transition动画
//             */
////            window.enterTransition = Hold()
////            window.exitTransition = Hold()
//            /**
//             * 3、设置ShareElementTransition,指定的ShareElement会执行这个Transiton动画
//             */
//            val transitionSet = TransitionSet()
//            transitionSet.addTransition(ChangeBounds())
//            transitionSet.addTransition(ChangeTransform())
//            transitionSet.addTarget(bga_banner)
////            transitionSet.duration = 100
//            window.sharedElementEnterTransition = transitionSet
//            window.sharedElementExitTransition = transitionSet
//        }

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))

//        val shaderParams = view_shader.layoutParams
//        shaderParams.height = statusHeight + dp2px(44)
//        view_shader.layoutParams = shaderParams

//        custom_coordinator.setmZoomView(view_holder)
//        custom_coordinator.setZoom(0f)
//        custom_coordinator.setmMoveView(toolbar, view_pager, con_header)
        state_pager_view.showLoading()
        initViewModel()
        initRecyclerView()
        initViewPager()
        initMagicIndicator()
        val params = view_top.layoutParams as? ConstraintLayout.LayoutParams
        params?.topMargin = statusHeight
        view_top.layoutParams = params


        //半秒回调一次
        audioPlayerManager.setSleep(500)
//        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
//            override fun prepared() {
//                logger.info("prepared")
//            }
//
//            override fun start() {
//                logger.info("start 总长=${audioPlayerManager.getDuration()}")
////                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
////                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
////                tv_time.setCompoundDrawables(drawable, null, null, null)
//                //不使用实际的值
////                currentPlayHomeRecomItem?.introduceVoiceLength = (audioPlayerManager.getDuration() / 1000)+1
////                AliplayerManager.soundOff()
//                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
//                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
//            }
//
//            override fun resume() {
//                logger.info("resume")
//                AliPlayerManager.soundOff()
////                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
////                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
////                tv_time.setCompoundDrawables(drawable, null, null, null)
//                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
//                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
//            }
//
//            override fun pause() {
//                logger.info("pause")
//                AliPlayerManager.soundOn()
////                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
////                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
////                tv_time.setCompoundDrawables(drawable, null, null, null)
//                val padding = dp2px(0)
//                sdv_voice_state.setPadding(padding, padding, padding, padding)
//                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
//            }
//
//            override fun stop() {
//                logger.info("stop")
//                AliPlayerManager.soundOn()
////                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
////                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
////                tv_time.setCompoundDrawables(drawable, null, null, null)
//                val padding = dp2px(0)
//                sdv_voice_state.setPadding(padding, padding, padding, padding)
//                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
//            }
//
//
//        })
        audioPlayerManager.setMediaPlayInfoListener(object : MediaPlayInfoListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int) {
                logger.info("onError mediaPlayer=${mp.hashCode()} what=$what extra=$extra")
            }

            override fun onCompletion(mediaPlayer: MediaPlayer?) {
                logger.info("onCompletion mediaPlayer=${mediaPlayer.hashCode()}")
                tv_time.text = "${mHomePageViewModel?.homeInfoBean?.value?.voice?.length}″"
            }

            override fun onBufferingUpdate(mediaPlayer: MediaPlayer?, i: Int) {
                logger.info("onBufferingUpdate mediaPlayer=${mediaPlayer.hashCode()} i=$i")
            }

            override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
                logger.info("onSeekComplete mediaPlayer=${mediaPlayer.hashCode()} ")
            }

            override fun onSeekBarProgress(progress: Int) {
//                logger.info("onSeekBarProgress progress=${progress / 1000}")
                val voiceLength = mHomePageViewModel?.homeInfoBean?.value?.voice?.length ?: return
                tv_time.text = "${voiceLength - progress / 1000}″"
            }
        })


        tv_time.setTFDinAltB()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)


        iv_more.onClickNew {
            //更多
            mHomePageActionFragment = mHomePageActionFragment ?: HomePageActionFragment()
            mHomePageActionFragment?.show(supportFragmentManager, "HomePageActionFragment")
        }

//        iv_more_black.onClickNew {
//            //更多操作
//            iv_more.performClick()
//        }

        iv_close.onClickNew {
            finish()
        }
//        iv_close_black.onClickNew {
//            finish()
//        }
//        custom_coordinator.mListener = object : ScrollMarginListener {
//            override fun scroll(distance: Int) {
//                val params = view_shader.layoutParams as? ConstraintLayout.LayoutParams ?: return
//                params.topMargin = distance
//                view_shader.layoutParams = params
//            }
//        }

        appBarLayout.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                val changeEnableDistance = abs(verticalOffset) - mStartChange
                when {
                    changeEnableDistance <= 0 -> {
                        tv_user_name.alpha = 0f
                        view_shader_white.alpha = 0f
                        iv_more.alpha = 1f
                        iv_more_black.alpha = 0f
                        iv_close.alpha = 1f
                        iv_close_black.alpha = 0f
                    }
                    changeEnableDistance >= mChangeDistance -> {
                        tv_user_name.alpha = 1f
                        view_shader_white.alpha = 1f
                        iv_more.alpha = 0f
                        iv_more_black.alpha = 1f
                        iv_close.alpha = 0f
                        iv_close_black.alpha = 1f
                    }
                    else -> {
                        val alpha = changeEnableDistance / mChangeDistance.toFloat()
                        tv_user_name.alpha = alpha
                        view_shader_white.alpha = alpha
                        iv_close_black.alpha = alpha
                        iv_more.alpha = (1 - alpha)
                        iv_more_black.alpha = alpha
                        tv_user_name.alpha = alpha
                    }
                }

            }
        })


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//
//            custom_coordinator.setOnScrollChangeListener { v: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
//                val changeEnableDistance = scrollY - mStartChange
//                when {
//                    changeEnableDistance <= 0 -> {
//                        tv_user_name.alpha = 0f
//                    }
//                    changeEnableDistance >= mChangeDistance -> {
//                        tv_user_name.alpha = 1f
//                    }
//                    else -> {
//                        val alpha = changeEnableDistance / mChangeDistance.toFloat()
//                        tv_user_name.alpha = alpha
//                    }
//                }
//            }
//        }
        //播放音效
        view_voice.onClickNew {
            //进入语音录制页面
            val voiceBean = mHomePageViewModel.homeInfoBean.value?.voice ?: return@onClickNew
            if (mHomePageViewModel.mineHomePage && voiceBean.voiceStatus.isEmpty()) {
                //录制语音
                val intent = Intent(this, VoiceSignActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    startActivity(intent)
                }
            }
        }


        view_private_chat.onClickNew {
            //私信页面
            val userId = mHomePageViewModel.targetUserId
            val bundle = Bundle()
            bundle.putLong(ParamConstant.TARGET_USER_ID, userId)
            val basicBean = mHomePageViewModel.homeInfoBean.value
            if (basicBean != null) {
                bundle.putString(ParamConstant.NICKNAME, basicBean.nickname)
                bundle.putString(ParamConstant.HeaderPic, basicBean.headPic)
            }
            ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY)
                .with(bundle)
                .navigation(this)
        }


        tv_time.onClickNew {
            if (SharedPreferencesUtils.getBoolean(SPParamKey.VOICE_ON_LINE, false)) {
                ToastUtils.show("正在语音通话，请稍后再试")
                return@onClickNew
            }
            val voiceBean = mHomePageViewModel.homeInfoBean.value?.voice ?: return@onClickNew
            if (mHomePageViewModel.mineHomePage && voiceBean.voiceStatus != VoiceBean.Pass) {
                //我的主页,语音为空，跳转编辑资料页面
//                RNPageActivity.start(this, RnConstant.EDIT_MINE_HOMEPAGE)
                val intent = Intent(this, EditInfoActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    startActivity(intent)
                }
                return@onClickNew
            }
            if (voiceBean.voiceStatus != VoiceBean.Pass) {
                return@onClickNew
            }
            //播放音效
            if (audioPlayerManager.musicType == -1 || audioPlayerManager.mediaPlayer == null) {
                //未设置音频地址
                audioPlayerManager.setNetPath(
                    StringHelper.getOssAudioUrl(
                        mHomePageViewModel.homeInfoBean.value?.voice?.voiceUrl ?: ""
                    )
                )
                audioPlayerManager.start(false)
            } else {
                //已设置音频地址
                if (audioPlayerManager.isPlaying) {
                    audioPlayerManager.pause()
                } else {
                    audioPlayerManager.resume()
                }
            }
        }
        sdv_voice_state.onClickNew {
            tv_time.performClick()
        }

//        view_living.onClickNew {
//            //跳转直播间
//            val programId =
//                mHomePageViewModel.homeInfoBean.value?.playProgram?.programId ?: return@onClickNew
//            PlayerActivity.start(this, programId, PlayerFrom.UserHome)
//        }
        tv_black_status.onClickNew {
            //屏蔽事件
        }
        rl_edit_info.onClickNew {
            //跳转编辑资料页面
//            RNPageActivity.start(this, RnConstant.EDIT_MINE_HOMEPAGE)
            val intent = Intent(this, EditInfoActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }

        view_heart.onClickNew {
            //心动
            mHomePageViewModel.like(mHomePageViewModel.targetUserId)
        }

        iv_mark.onClickNew {
            val bean = mHomePageViewModel.homeInfoBean.value ?: return@onClickNew
            //用户类型
            val userType = bean.userType
            if (userType == UserType.Manager) {
                //官方没有点击效果
                return@onClickNew
            }
            if (bean.realNameGuide?.guide == true) {
                //实名，显示实名弹窗
                realNameNotice()
                return@onClickNew
            }
            if (bean.realHeadGuide?.guide == true) {
                //实人
                realHeaderNotice()
                return@onClickNew
            }

        }

        rl_guide_photo.onClickNew {
            val intent = Intent(this, EditInfoActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }
    }


    private fun initViewPager() {
        mPagerAdapter = HomePageAdapter(supportFragmentManager, mHomePageViewModel.targetUserId)
        view_pager.adapter = mPagerAdapter
        //配置预加载页数
//        view_pager.offscreenPageLimit = 2
        view_pager.currentItem = 0

    }

    /**
     * 初始化历史记录指示器
     */
    private fun initMagicIndicator() {
        mCommonNavigator = CommonNavigator(this)
        mCommonNavigator.scrollPivotX = 0.65f
//        mCommonNavigator.isAdjustMode = true
        mCommonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return mTabTitles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {

                val simplePagerTitleView: ScaleTransitionPagerTitleView =
                    ScaleTransitionPagerTitleView(context)
                simplePagerTitleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                simplePagerTitleView.minScale = 0.7f
                simplePagerTitleView.text = mTabTitles[index]
                simplePagerTitleView.textSize = 20f
                simplePagerTitleView.normalColor =
                    ContextCompat.getColor(context, R.color.black_666)
                simplePagerTitleView.selectedColor =
                    ContextCompat.getColor(context, R.color.black_333)
                simplePagerTitleView.setOnClickListener { view_pager.currentItem = index }
                if (view_pager.currentItem == index) {
                    simplePagerTitleView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_333
                        )
                    )
                    simplePagerTitleView.scaleX = 1.0f
                    simplePagerTitleView.scaleY = 1.0f
                } else {
                    simplePagerTitleView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_666
                        )
                    )
                    simplePagerTitleView.scaleX = 0.7f
                    simplePagerTitleView.scaleY = 0.7f
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
//                if (mTabTitles.size <= 1) {
//                    return null
//                }
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = dp2pxf(3)
                indicator.lineWidth = dp2pxf(6)
                indicator.roundRadius = dp2pxf(2)
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.yOffset = 12f
                indicator.setColors(context.resources.getColor(R.color.primary_color))
                return indicator
            }
        }
        magic_indicator.navigator = mCommonNavigator
        ViewPagerHelper.bind(magic_indicator, view_pager)
    }


    /**
     * 重置页面
     */
    private fun resetView(intent: Intent) {
        mShowPic = intent.getStringExtra(ParamConstant.ShowPic) ?: ""

        val userID = intent.getLongExtra(ParamConstant.UserId, 0)
        mHomePageViewModel.targetUserId = userID
        //是否是我的主页
        mHomePageViewModel.mineHomePage = userID == SessionUtils.getUserId()
        if (mHomePageViewModel.mineHomePage) {
//            iv_more_black.hide()
            iv_more.hide()
        } else {
//            iv_more_black.show()
            iv_more.show()
        }
        mHomePageViewModel.homeInfo()


    }


    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView_piclist.layoutManager = AutoCenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView_piclist.adapter = mPicAdapter

        mPicAdapter.setOnItemClickListener { adapter, view, position ->
            selectPic(position)
        }

    }

    private fun initViewModel() {
        mHomePageViewModel.loadState.observe(this, Observer {
            if (it != null) {
                when (it.state) {
                    NetStateType.SUCCESS -> {
                        state_pager_view.showSuccess()
//                        mPagerAdapter?.notifyDataSetChanged()
                    }
                    NetStateType.NETWORK_ERROR -> {
                        ToastUtils.show("网络异常")
                        state_pager_view.showError(btnClick = View.OnClickListener { mHomePageViewModel.homeInfo() })
                    }
                    NetStateType.LOADING -> {
                        state_pager_view.showLoading()
                    }


                }
            }
        })
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }

//            con_header.post {
//                val holderParams = view_holder.layoutParams
//                holderParams.height = con_header.height - 1
//                view_holder.layoutParams = holderParams
//            }
        })


        mHomePageViewModel.closeListData.observe(this, Observer {
            if (it != null) {
                //显示亲密榜单
                mIntimacyFragment.show(supportFragmentManager, "IntimacyFragment")
            }
        })

        mHomePageViewModel.blackStatus.observe(this, Observer {
            if (it != null) {
                //我是否被对方拉黑
                val targetBlack = mHomePageViewModel.homeInfoBean.value?.targetBlack ?: ""
                if (it == BusiConstant.True || targetBlack == BusiConstant.True) {
                    //拉黑关系
                    tv_black_status.show()
                } else {
                    tv_black_status.hide()
                }
                if (it == BusiConstant.True) {
                    //我拉黑对方
                    if (targetBlack == BusiConstant.True) {
                        //互相拉黑
                        tv_black_status.text = "已添加至黑名单，不能互发消息和心动"
                    } else {
                        //我把对方拉黑
                        tv_black_status.text = "已添加至黑名单，不能互发消息和心动"
                    }
                } else {
                    if (targetBlack == BusiConstant.True) {
                        //被对方拉黑
                        tv_black_status.text = "已被对方添加至黑名单，不能互发消息和心动"
                    }
                }
            }
        })

        mHomePageViewModel.actionData.observe(this, Observer {
            if (it != null) {
                when (it) {
                    HomePageViewModel.ACTION_BLACK -> {
                        //拉黑

                        if (mHomePageViewModel.blackStatus.value == BusiConstant.True) {
                            //解除黑名单
//                            MyAlertDialog(
//                                this
//                            ).showAlertWithOKAndCancel(
//                                "将对方移除黑名单",
//                                MyAlertDialog.MyDialogCallback(onRight = {
//                                    //拉黑
//                                    if (mHomePageViewModel.blackStatus.value == BusiConstant.True) {
//                                        mHomePageViewModel?.recover()
//                                    }
//
//                                }), "移除黑名单", "确定", "取消"
//                            )
                            mHomePageViewModel?.recover()
                            return@Observer
                        } else {
                            MyAlertDialog(
                                this
                            ).showAlertWithOKAndCancel(
                                "加入黑名单，你们将相互不能给对方发送消息",
                                MyAlertDialog.MyDialogCallback(onRight = {
                                    //拉黑
                                    if (mHomePageViewModel.blackStatus.value == BusiConstant.False) {
                                        mHomePageViewModel?.black()
                                    }

                                }), "加入黑名单", "确定", "取消"
                            )
                            return@Observer
                        }
                    }
                    HomePageViewModel.ACTION_REPORT -> {
                        //举报
                        val extra = Bundle()

                        extra.putLong(
                            ParamConstant.TARGET_USER_ID,
                            mHomePageViewModel?.homeInfoBean?.value?.userId ?: return@Observer
                        )

                        ARouter.getInstance().build(ARouterConstant.REPORT_ACTIVITY).with(extra)
                            .navigation()
                    }
                }
            }

        })

        mHomePageViewModel.evaluateTagsBean.observe(this, Observer {
            if (it != null) {
                //显示评论弹窗
                mHomePageEvaluateFragment = mHomePageEvaluateFragment ?: HomePageEvaluateFragment()
                mHomePageEvaluateFragment?.show(supportFragmentManager, "HomePageEvaluateFragment")
            }
        })
        mHomePageViewModel.appraiseListData.observe(this, Observer {
            if (it != null) {
            }
        })

        mHomePageViewModel.heartStatus.observe(this, Observer {
            if (it != null) {
                //执行伸长动画
                showHeartView(it)
                showGrowthAnimation()
            }
        })

        mHomePageViewModel.realPeopleState.observe(this, Observer {
            if (it == true) {
                realHeader()
            }
        })

        huanQueViewModel.userInfoStatusChange.observe(this, Observer {
            if (it.isSuccess()) {
                val change = it.requireT()
                if (change.userId == mHomePageViewModel.targetUserId) {
                    mHomePageViewModel.followStatus.value = change.follow
                }
            }
        })
    }

    private var cSeeMaxCoverNum: Int = -1

    /**
     * 显示页面
     */
    private fun showViewByData(bean: HomePageInfo) {
        cSeeMaxCoverNum = bean.seeMaxCoverNum
        tv_user_name.text = bean.nickname
        if (!mHomePageViewModel.shareElement) {
            showPic(bean.headPic, bean.picList, bean.seeMaxCoverNum)
        }
        tv_nickname.text = bean.nickname
        //显示图标
        //是否是真人
        val headRealPeople = bean.headRealPeople
        //是否实名
        val realName = bean.realName
        //用户类型
        val userType = bean.userType
        val userIcon = AppHelper.getUserIcon(headRealPeople == BusiConstant.True, realName, userType)
        if (userIcon == null) {
            iv_mark.hide()
        } else {
            iv_mark.show()
            iv_mark.setImageResource(userIcon)
//            sdv_real.loadImage(bean.authMark, 18f, 18f)
        }
        if (mHomePageViewModel.mineHomePage && bean.perfectGuide?.guide == true) {
            rl_guide_info.show()
            rl_guide_info.backgroundColor= Color.parseColor("#CACED7")
            tv_guide_title.text = bean.perfectGuide?.guideText
            tv_guide_title_02.show()
            tv_guide_title_02.text = "资料完整度：${bean.perfection}%"
            tv_guide_title.setCompoundDrawables(null, null, null, null)
            rl_guide_info.onClickNew {
                val intent = Intent(this, EditInfoActivity::class.java)
                if (ForceUtils.activityMatch(intent)) {
                    startActivity(intent)
                }
            }
        } else if (bean.realNameGuide?.guide == true) {
            rl_guide_info.show()
            rl_guide_info.backgroundColor= Color.parseColor("#F3C060")
            tv_guide_title.text = "Ta已完成实名认证，加速推荐中"
            val drawable = ContextCompat.getDrawable(this, R.mipmap.icon_title_realname)
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                tv_guide_title.setCompoundDrawables(drawable, null, null, null)
            }
            tv_guide_title_02.hide()
            rl_guide_info.onClickNew {
                realNameNotice()
            }
        } else if (bean.realHeadGuide?.guide == true) {
            rl_guide_info.show()
            rl_guide_info.backgroundColor= Color.parseColor("#F3C060")
            tv_guide_title.text = "Ta已完成真人认证，可放心交友"
            val drawable = ContextCompat.getDrawable(this, R.mipmap.icon_auth_head_success)
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                tv_guide_title.setCompoundDrawables(drawable, null, null, null)
            }
            tv_guide_title_02.hide()
            rl_guide_info.onClickNew {
                realHeaderNotice()
            }
        } else {
            rl_guide_info.hide()
        }

        val onLineBean = bean.online
        if (onLineBean == null) {
            tv_online.hide()
        } else {
            tv_online.show()
            if (onLineBean.onlineStatus == "Online") {
                tv_online.text = "在线"
                tv_online.isSelected = true
            } else {
                if (onLineBean.onlineStatusText.isNotEmpty()) {
                    tv_online.text = onLineBean.onlineStatusText
                    tv_online.isSelected = false
                } else {
                    tv_online.hide()
                }
            }
        }


        if (bean.interactTips.isNotEmpty()) {
            tv_xd_time.text = bean.interactTips
            tv_xd_time.show()
        } else {
            tv_xd_time.hide()
        }

        val voiceStatus = bean.voice.voiceStatus
        if (voiceStatus == VoiceBean.Pass) {
            //审核通过 显示语音签名
            tv_time.text = "${bean.voice.length}″"
            view_voice.show()
            tv_time.show()
            sdv_voice_state.show()
            tv_record.hide()
//            sdv_voice_state.backgroundResource = R.drawable.bg_enable
//            ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
        } else {
            if (mHomePageViewModel.mineHomePage) {
                //我的主页
                if (voiceStatus != VoiceBean.Pass) {
                    //语音签名为空 显示待录制状态
                    view_voice.show()
                    tv_record.show()
                    tv_time.hide()
                    sdv_voice_state.hide()
                    when {
                        voiceStatus == VoiceBean.Wait -> {
                            //审核中
                            tv_record.text = "语音审核中"
                        }
                        voiceStatus == VoiceBean.Reject -> {
                            //已拒绝
                        }
                        voiceStatus.isEmpty() -> {
                            //未录制
                            tv_record.text = "语音录制"
                        }
                    }
                }
            } else {
                //他人主页 不显示语音签名
                view_voice.hide()
                tv_time.hide()
                sdv_voice_state.hide()
                tv_record.hide()
            }

        }



        if (bean.mySign.isEmpty()) {
            tv_sign_home.hide()
        } else {
            tv_sign_home.show()
        }
        tv_sign_home.text = bean.mySign
        val postNum = bean.postNum
        val postStr =
            if (postNum > 0) {
                "动态 $postNum"
            } else {
                "动态"
            }

        if (ForceUtils.isIndexNotOutOfBounds(1, mTabTitles)) {
            mTabTitles.removeAt(1)
            mTabTitles.add(postStr)
            mCommonNavigator.adapter?.notifyDataSetChanged()
        }
        if (mHomePageViewModel.mineHomePage) {
            tv_distance.hide()
            iv_vehicle.hide()

        } else {


            val distance = bean.distanceCity.distance
            if (bean.distanceCity.curryCityName.isEmpty()) {
                //没有城市，显示星球
                val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
                val currentStar = starList.random()
                tv_distance.text = currentStar
                iv_vehicle.show()
                iv_vehicle.imageResource = R.mipmap.icon_home_distance_rocket
            } else {
                //有城市
                if (bean.distanceCity.sameCity == BusiConstant.True) {
                    //同市
                    iv_vehicle.hide()
                    if (distance >= 1000) {
                        val df = DecimalFormat("#.0")
                        df.roundingMode = RoundingMode.DOWN
                        tv_distance.text = "${df.format(distance / 1000.0)}km ${bean.distanceCity.curryCityName} /"
                    } else {
                        tv_distance.text = "${distance}m ${bean.distanceCity.curryCityName} /"
                    }
                } else {
                    //不同市
                    iv_vehicle.show()
                    tv_distance.text = ""
                    if (distance < 100 * 1000) {
                        //显示汽车
                        iv_vehicle.imageResource = R.mipmap.icon_home_distance_car
                    } else if (distance > 800 * 1000) {
                        //显示飞机
                        iv_vehicle.imageResource = R.mipmap.icon_home_distance_air_plan
                    } else {
                        //显示动车
                        iv_vehicle.imageResource = R.mipmap.icon_home_distance_rail_way
                    }
                }

            }
        }

        val sexName = if (bean.sex == Sex.MALE) {
            "男"
        } else {
            "女"
        }
        val age = bean.age
        if (age > 0) {
            tv_age.text = "${bean.age}岁 ${sexName}"
        } else {
            tv_age.text = "${sexName}"
        }

        showHeartView(bean.heartTouch)
        if (mHomePageViewModel.mineHomePage) {
            rl_edit_info.show()
            //隐藏底部
            view_private_chat.hide()
            tv_private_chat.hide()
            tv_black_status.hide()
            tv_home_heart.hide()
        } else {
            rl_edit_info.hide()
            //显示底部视图
            view_private_chat.show()
            tv_private_chat.show()
            tv_home_heart.show()
        }
    }

    /**
     * 显示图片数据
     */
    private fun showPic(
        headerPic: String,
        coverPicList: MutableList<String>,
        freeCount: Int
    ) {
        val picList = mutableListOf<HomePagePicBean>()
        if (headerPic.isNotEmpty()) {
            picList.add(HomePagePicBean(headerPic, selected = BusiConstant.True, blur = false))
        }
        coverPicList.forEachIndexed { index, cover ->
            val mBlur = if (freeCount == -1) {
                false
            } else {
                index >= freeCount - 1
            }
            picList.add(HomePagePicBean(cover, blur = mBlur))
        }
        showBanner(picList)
        mPicAdapter.setList(picList)
        if (mShowPic.isNotEmpty()) {
            //定位到特定的图片
            picList.forEachIndexed { index, homePagePicBean ->
                if (homePagePicBean.coverPic == mShowPic) {
                    //找到需要选中的图片
                    bga_banner.currentItem = index
                    return
                }
            }
        }
    }


    /**
     * 显示心动状态
     */
    private fun showHeartView(heartStatus: String) {
        if (!mHomePageViewModel.mineHomePage) {
            if (heartStatus == BusiConstant.True) {
                //已经心动
                view_heart.hide()
                tv_home_heart.hide()
            } else {
                //未心动
                view_heart.show()
                tv_home_heart.show()
            }
        }
    }

    //伸长动画
    private var mGrowthAnimation: ValueAnimator? = null

    /**
     * 伸长动画
     */
    private fun showGrowthAnimation() {
        val startWidth = view_private_chat.width
        val targetWidth = ScreenUtils.getScreenWidth() - dp2px(30)
        mGrowthAnimation?.cancel()
        mGrowthAnimation = ValueAnimator.ofFloat(startWidth.toFloat(), targetWidth.toFloat())
        mGrowthAnimation?.apply {
            duration = 200
        }
        mGrowthAnimation?.addUpdateListener {
            val tempWidth = it.animatedValue as? Float ?: return@addUpdateListener
            logger.info("Animation 当前数值:${tempWidth}")
            val heartParams = view_private_chat.layoutParams as? ConstraintLayout.LayoutParams
            heartParams?.width = tempWidth.toInt()
            view_private_chat.layoutParams = heartParams
        }
        mGrowthAnimation?.start()

    }


    private val bannerAdapter by lazy {
        BGABanner.Adapter<View, HomePagePicBean> { _, itemView, model, _ ->
            val pic = itemView?.findViewById<SimpleDraweeView>(R.id.sdv) ?: return@Adapter

            if (model?.blur == true) {
//                ImageUtils.loadImageWithBlur(pic, model.coverPic, 3, 50)
                ImageUtils.loadImageNoResize(pic, "${model.coverPic}${BusiConstant.OSS_BLUR_01}")
            } else {
                ImageUtils.loadImageNoResize(pic, "${model?.coverPic}")
            }
//            ImageUtils.loadImageNoResize(pic, "${model?.coverPic}")

//            val icWater = itemView?.findViewById<ImageView>(R.id.iv_water) ?: return@Adapter
//            if (model?.realPic == BusiConstant.True) {
//                icWater.show()
//            } else {
//                icWater.hide()
//            }
        }
    }

    private val bannerItemClick by lazy {
        BGABanner.Delegate<View, HomePagePicBean> { _, _, model, posisiton ->
            val picBeanList = mPicAdapter.data
            val picList = mutableListOf<String>()
            picBeanList.forEach {
                val pic =
                    if (it.blur) {
                        it.coverPic + BusiConstant.OSS_BLUR_01
                    } else {
                        it.coverPic
                    }
                picList.add(StringHelper.getOssImgUrl(pic))
            }
//            val bean = mHomePageViewModel.homeInfoBean.value ?: return@Delegate
//            val imageList = bean.picList.apply { add(bean.headPic) }
//
//            imageList.forEach { picList.add(StringHelper.getOssImgUrl(it)) }
            logger.info("State = ${lifecycle.currentState}")
            val freeCount = mHomePageViewModel.homeInfoBean.value?.seeMaxCoverNum ?: -1
            //&& !custom_coordinator.isScrolling()
            if (!isFinishing) {
                mHomePageViewModel.needRefresh = false
                ImageActivity.start(
                    this, posisiton, picList,
                    from = ImageActivityFrom.HOME,
                    operate = ImageActivityOperate.REPORT,
                    userId = mHomePageViewModel.targetUserId
                )
            }
        }
    }

    /**
     *
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveImagePosition(event: ImagePositionEvent) {
        logger("收到查看图片的位置的消息：${event.position}")
        if (event.position != -1) {
            bga_banner.currentItem = event.position
        }

    }

    /**
     * 显示banner数据
     */
    private fun showBanner(picList: MutableList<HomePagePicBean>) {
        bga_banner.setAdapter(bannerAdapter)
        bga_banner.setDelegate(bannerItemClick)
        bga_banner.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectPic(position, true)
            }

        })
        bga_banner.setData(R.layout.item_bga_banner_image_home_page, picList, null)
        bga_banner.setAutoPlayAble(false)
//        bga_banner.startAutoPlay()
        bga_banner.currentItem = 0
    }

    /**
     * 选中特定的图片
     */
    private fun selectPic(position: Int, fromBga: Boolean = false) {
        val dataList = mPicAdapter.data
        dataList.forEachIndexed { index, homePagePicBean ->
            if (index == position) {
                homePagePicBean.selected = BusiConstant.True
            } else {
                homePagePicBean.selected = BusiConstant.False
            }
        }
        mPicAdapter.notifyDataSetChanged()
        if (!fromBga) {
            bga_banner.currentItem = position
        }
        if (position >= 0) {
            val manager = recyclerView_piclist.layoutManager as AutoCenterLayoutManager
            manager.smoothScrollToPosition(recyclerView_piclist, position)
//            recyclerView_piclist.smoothScrollToPosition(position)
        }

        if (cSeeMaxCoverNum != -1) {
            if (position >= cSeeMaxCoverNum) {
                rl_guide_photo.show()
            } else {
                rl_guide_photo.hide()
            }
        } else {
            rl_guide_photo.hide()
        }
    }

    /**
     * 更新乐园和直播状态的大小
     */
    private fun updateLeYuanAndProgramParams() {
//        val tempWidth = (ScreenUtils.getScreenWidth() - dp2px(15 * 3)) / 2
//        val tempHeight = tempWidth * 70 / 165
//        val coverParams = sdv_cover.layoutParams
//        coverParams.width = tempWidth
//        coverParams.height = tempHeight
//        sdv_cover.layoutParams = coverParams
    }

    /**
     * 显示勋章数据
     */
//    private fun showGuanfang(badges: MutableList<String>) {
//        if (badges.isEmpty()) {
//            //没有勋章，隐藏
//            stv_medal.hide()
//        } else {
//            val list = arrayListOf<TIBean>()
//            stv_medal.show()
//            badges.forEach {
//                if (!TextUtils.isEmpty(it)) {
//                    val image = TIBean()
//                    image.type = 1
//                    image.url = StringHelper.getOssImgUrl(it)
//                    image.height = dp2px(18)
//                    image.width = dp2px(66)
//                    list.add(image)
//                }
//            }
//            stv_medal.movementMethod = LinkMovementMethod.getInstance();
//            val textBean = ImageUtils.renderTextAndImage(list, "   ")
//            stv_medal.renderBaseText(textBean ?: return) { builder ->
//                badges.forEachIndexed { index, data ->
//                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
//                        override fun onClick(textView: View) {
//                            if (mHomePageViewModel.mineHomePage) {
//                                doWithMedalAction(data)
//                            }
//                        }
//
//                        override fun updateDrawState(ds: TextPaint) {
//                            super.updateDrawState(ds)
//                            ds.isUnderlineText = false
//                        }
//                    }
//
//                    builder.setSpan(
//                        clickableSpan, index + 3 * index,
//                        index + 3 * index + 1,
//                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                }
//
//
//            }
//
//        }
//    }

    /**
     * 处理勋章跳转
     */
    private fun doWithMedalAction(data: String) {
        val params = GlobalUtils.uri2Map(data)
        val touchType = params["touchType"] ?: ""
        when (touchType) {
            "RealHead" -> {
                if (mHomePageViewModel.homeInfoBean.value?.headRealPeople == BusiConstant.True) {
                    ToastUtils.show("已完成真人认证")
                } else {
//                    MyAlertDialog(this).showAlertWithOKAndCancel(
//                        "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
//                        MyAlertDialog.MyDialogCallback(onRight = {
//                            (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
//                                .navigation() as? IRealNameService)?.checkRealHead { e ->
//                                if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
//                                    MyAlertDialog(this, false).showAlertWithOKAndCancel(
//                                        e.busiMessage.toString(),
//                                        title = "修改提示",
//                                        okText = "修改头像",
//                                        noText = "取消",
//                                        callback = MyAlertDialog.MyDialogCallback(onRight = {
//                                            RNPageActivity.start(
//                                                this,
//                                                RnConstant.EDIT_MINE_HOMEPAGE
//                                            )
//                                        })
//                                    )
//
//                                }
//                            }
//                        }), "真人照片未认证", okText = "去认证", noText = "取消"
//                    )
                    realHeaderNotice()
                }
            }
            "UserLevel" -> {
                //财富等级页面
                RNPageActivity.start(this, RnConstant.WEALTH_LEVEL_PAGE)
            }
            "RoyalLevel" -> {
                //贵族等级页面
                RNPageActivity.start(this, RnConstant.ROYAL_PAGE)
            }
            "AnchorLevel" -> {
                //主播等级页面
                RNPageActivity.start(this, RnConstant.ANCHOR_LEVEL_PAGE)
            }
            else -> {
            }
        }
    }

    private fun realHeaderNotice() {
        CommonDialogFragment.create(
            title = "真人照片认证",
            content = "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
            imageRes = R.mipmap.bg_dialog_real_auth,
            okText = "去认证",
            cancelText = "取消",
            callback = CommonDialogFragment.Callback(
                onOk = {
                    (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                        .navigation() as? IRealNameService)?.checkRealHead { e ->
                        if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                            MyAlertDialog(this, false).showAlertWithOKAndCancel(
                                e.busiMessage.toString(),
                                title = "修改提示",
                                okText = "修改头像",
                                noText = "取消",
                                callback = MyAlertDialog.MyDialogCallback(onRight = {
                                    val intent = Intent(this, EditInfoActivity::class.java)
                                    if (ForceUtils.activityMatch(intent)) {
                                        startActivity(intent)
                                    }
                                })
                            )
                        }
                    }

                }
            )
        ).show(this, "CommonDialogFragment")

    }

    private fun realNameNotice() {
        CommonDialogFragment.create(
            title = "实名认证",
            content = "完成实名认证，提高真人交友可信度，将获得更多推荐机会~",
            imageRes = R.mipmap.bg_dialog_real_name,
            okText = "去认证",
            cancelText = "取消",
            callback = CommonDialogFragment.Callback(
                onOk = {
                    ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY)
                        .navigation()
                }
            )
        ).show(this, "CommonDialogFragment")

    }

    /**
     * 实人认证
     */
    private fun realHeader() {

        (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
            .navigation() as? IRealNameService)?.checkRealHead { e ->
            if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                MyAlertDialog(this, false).showAlertWithOKAndCancel(
                    e.busiMessage.toString(),
                    title = "修改提示",
                    okText = "修改头像",
                    noText = "取消",
                    callback = MyAlertDialog.MyDialogCallback(onRight = {
                        val intent = Intent(this, EditInfoActivity::class.java)
                        if (ForceUtils.activityMatch(intent)) {
                            startActivity(intent)
                        }
                    })
                )

            }
        }
    }


    /**
     * 设置地图 位置距离上方距离
     */
    private fun setMapMargin(view: View, margin: Int) {
        val params = view.layoutParams as? ConstraintLayout.LayoutParams
        params?.topMargin = margin
        view.layoutParams = params
    }


    /**
     * 检查录音权限
     */
    private fun checkAudioPermission() {
        val rxPermissions = RxPermissions(this)
        rxPermissions.requestEachCombined(Manifest.permission.RECORD_AUDIO)
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger("获取权限成功")
                        val userId = mHomePageViewModel.targetUserId
                        val bundle = Bundle()
                        bundle.putLong(ParamConstant.UserId, userId)
                        bundle.putString(
                            ParamConstant.TYPE,
                            ConmmunicationUserType.CALLING
                        )
                        //                        bundle.putString(ParamConstant.NICKNAME, nickname);
//                        intent.putExtra(ParamConstant.MEET_STATUS, meetStatus)
//                                        bundle.putString(ParamConstant.OPERATION, OperationType.CALL_PHONE)
                        ARouter.getInstance()
                            .build(ARouterConstant.VOICE_CHAT_ACTIVITY).with(bundle)
                            .navigation(this)
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        ToastUtils.show("权限无法获取")
                    else -> {
                        logger("获取权限被永久拒绝")
                        val message = "无法获取到录音权限，请手动到设置中开启"
                        ToastUtils.show(message)
                    }
                }

            }
    }

    override fun onRestart() {
        super.onRestart()
        if (!mHomePageViewModel.needRefresh) {
            mHomePageViewModel.needRefresh = true
        } else {
            //重新获取数据
//        if (mHomePageViewModel.mineHomePage) {
            mHomePageViewModel.homeInfo()
//        }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mGrowthAnimation?.cancel()
        audioPlayerManager.destroy()
    }

    override fun onViewDestroy() {
        super.onViewDestroy()
//        if (mShowPic.isEmpty()) {
//            return
//        }
//        val userId = mHomePageViewModel.targetUserId
//        var tempIndex = -1
//        var tempUrl = ""
//        mPicAdapter.data.forEachIndexed { index, homePagePicBean ->
//            if (homePagePicBean.selected == BusiConstant.True) {
//                //正在展示的图片
//                tempIndex = index
//                tempUrl = homePagePicBean.coverPic
//            }
//        }
//        if (mShowPic.isNotEmpty() && tempUrl != mShowPic) {
//            if (userId > 0 && tempIndex >= 0) {
//                EventBus.getDefault().post(PicChangeEvent(mHomePageViewModel.targetUserId, tempUrl, tempIndex))
//            }
//        }
    }

    override fun finish() {
        super.finish()
        if (mShowPic.isEmpty()) {
            return
        }
        val userId = mHomePageViewModel.targetUserId
        var tempIndex = -1
        var tempUrl = ""
        mPicAdapter.data.forEachIndexed { index, homePagePicBean ->
            if (homePagePicBean.selected == BusiConstant.True) {
                //正在展示的图片
                tempIndex = index
                tempUrl = homePagePicBean.coverPic
            }
        }
        if (mShowPic.isNotEmpty() && tempUrl != mShowPic) {
            if (userId > 0 && tempIndex >= 0) {
                EventBus.getDefault().post(PicChangeEvent(mHomePageViewModel.targetUserId, tempUrl, tempIndex))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            resetView(intent)
        }
    }
}

