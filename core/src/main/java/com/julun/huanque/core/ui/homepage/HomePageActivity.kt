package com.julun.huanque.core.ui.homepage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.ChangeTransform
import android.transition.TransitionSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.transition.platform.Hold
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.interfaces.ScrollMarginListener
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.manager.HuanViewModelManager
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.ui.image.ImageActivity
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.common.widgets.indicator.ScaleTransitionPagerTitleView
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePageAdapter
import com.julun.huanque.core.adapter.HomePagePicListAdapter
import com.julun.huanque.core.manager.AliPlayerManager
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
import org.jetbrains.anko.*
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
        fun newInstance(act: Activity, userId: Long) {
            val intent = Intent(act, HomePageActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                intent.putExtra(ParamConstant.UserId, userId)
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

    override fun getLayoutId() = R.layout.act_home_page

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        intent?.let { tent ->
            resetView(tent)
        }
        val statusHeight = StatusBarUtil.getStatusBarHeight(this)
        mStartChange = ScreenUtils.getScreenWidth() * 308 / 375 - dp2px(44) - statusHeight

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /**
             * 1、设置相同的TransitionName
             */
            ViewCompat.setTransitionName(bga_banner, "Image${mHomePageViewModel.targetUserId}")
//            ViewCompat.setTransitionName(tv_nickname, "TextView${mHomePageViewModel.targetUserId}")
            /**
             * 2、设置WindowTransition,除指定的ShareElement外，其它所有View都会执行这个Transition动画
             */
//            window.enterTransition = Hold()
//            window.exitTransition = Hold()
            /**
             * 3、设置ShareElementTransition,指定的ShareElement会执行这个Transiton动画
             */
            val transitionSet = TransitionSet()
            transitionSet.addTransition(ChangeBounds())
            transitionSet.addTransition(ChangeTransform())
            transitionSet.addTarget(bga_banner)
//            transitionSet.duration = 100
            window.sharedElementEnterTransition = transitionSet
            window.sharedElementExitTransition = transitionSet
        }

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))

//        val shaderParams = view_shader.layoutParams
//        shaderParams.height = statusHeight + dp2px(44)
//        view_shader.layoutParams = shaderParams

        custom_coordinator.setmZoomView(view_holder)
        custom_coordinator.setZoom(0f)
        custom_coordinator.setmMoveView(toolbar, view_pager, con_header)

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
                tv_time.text = "${mHomePageViewModel?.homeInfoBean?.value?.voice?.length}s"
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
                tv_time.text = "${voiceLength - progress / 1000}s"
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
        custom_coordinator.mListener = object : ScrollMarginListener {
            override fun scroll(distance: Int) {
                val params = view_shader.layoutParams as? ConstraintLayout.LayoutParams ?: return
                params.topMargin = distance
                view_shader.layoutParams = params
            }
        }

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
                RNPageActivity.start(this, RnConstant.EDIT_MINE_HOMEPAGE)
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
                simplePagerTitleView.minScale = 0.583f
                simplePagerTitleView.text = mTabTitles[index]
                simplePagerTitleView.textSize = 24f
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
                    simplePagerTitleView.scaleX = 0.583f
                    simplePagerTitleView.scaleY = 0.583f
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator? {
//                if (mTabTitles.size <= 1) {
//                    return null
//                }
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = 12f
                indicator.lineWidth = 39f
                indicator.roundRadius = 6f
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
        //附近数据
        val nearByBean = intent.getSerializableExtra(ParamConstant.NearByBean) as? NearbyUserBean
        val favoriteBean = intent.getSerializableExtra(ParamConstant.FavoriteUserBean) as? FavoriteUserBean
//        mHomePageViewModel.nearByBeanData.value = nearByBean
        when {
            nearByBean != null -> {
                mHomePageViewModel.shareElement = true
                showPic("", nearByBean.coverPicList)
            }
            favoriteBean != null -> {
                mHomePageViewModel.shareElement = true
                showPic("", favoriteBean.coverPicList)
            }
            else -> {
                mHomePageViewModel.shareElement = false
            }
        }


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
        recyclerView_piclist.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
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
                        mPagerAdapter?.notifyDataSetChanged()
                    }
                    NetStateType.NETWORK_ERROR -> {
//                        state_pager_view.showError()
                    }
                    NetStateType.LOADING -> {
//                        state_pager_view.showLoading()
                    }


                }
            }
        })
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            if (it != null) {
                showViewByData(it)
            }

            con_header.post {
                val holderParams = view_holder.layoutParams
                holderParams.height = con_header.height - 1
                view_holder.layoutParams = holderParams
            }
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
                showHeartView(it)
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

    /**
     * 显示页面
     */
    private fun showViewByData(bean: HomePageInfo) {
        tv_user_name.text = bean.nickname
        if (!mHomePageViewModel.shareElement) {
            showPic(bean.headPic, bean.picList)
        }
        tv_nickname.text = bean.nickname
        if (bean.authMark.isEmpty()) {
            sdv_real.hide()
        } else {
            sdv_real.show()
//            sdv_real.loadImage(bean.authMark, 18f, 18f)
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
            if (distance >= 1000) {
                val df = DecimalFormat("#.00")
                tv_distance.text = "${df.format(distance / 1000)}km"
            } else {
                tv_distance.text = "${distance}m"
            }
            if (bean.distanceCity.sameCity == BusiConstant.True) {
                //同市
                iv_vehicle.hide()
            } else {
                //不同市
                iv_vehicle.show()
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


        val sexName = if (bean.sex == Sex.MALE) {
            "男"
        } else {
            "女"
        }
        val age = bean.age
        if (age > 0) {
            tv_age.text = "${bean.distanceCity.curryCityName} /${bean.age}岁 ${sexName}"
        } else {
            tv_age.text = "${bean.distanceCity.curryCityName} /${sexName}"
        }


        if (mHomePageViewModel.mineHomePage) {
            rl_edit_info.show()
            //隐藏底部
            view_private_chat.hide()
            tv_private_chat.hide()
            tv_black_status.hide()
            tv_home_heart.hide()
            view_heart.hide()
        } else {
            rl_edit_info.hide()
            //显示底部视图
            view_private_chat.show()
            tv_private_chat.show()
            tv_home_heart.show()
            view_heart.show()
        }
    }

    /**
     * 显示图片数据
     */
    private fun showPic(headerPic: String, coverPicList: MutableList<String>) {
        val picList = mutableListOf<HomePagePicBean>()
        if (headerPic.isNotEmpty()) {
            picList.add(HomePagePicBean(headerPic, selected = BusiConstant.True))
        }
        coverPicList.forEach {
            picList.add(HomePagePicBean(it))
        }
        showBanner(picList)
        mPicAdapter.setList(picList)
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


    private val bannerAdapter by lazy {
        BGABanner.Adapter<View, HomePagePicBean> { _, itemView, model, _ ->
            val pic = itemView?.findViewById<SimpleDraweeView>(R.id.sdv) ?: return@Adapter

            ImageUtils.loadImageNoResize(pic, "${model?.coverPic}")
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
                picList.add(StringHelper.getOssImgUrl(it.coverPic))
            }
//            val bean = mHomePageViewModel.homeInfoBean.value ?: return@Delegate
//            val imageList = bean.picList.apply { add(bean.headPic) }
//
//            imageList.forEach { picList.add(StringHelper.getOssImgUrl(it)) }
            logger.info("State = ${lifecycle.currentState}")
            if (!isFinishing && !custom_coordinator.isScrolling()) {
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
            recyclerView_piclist.smoothScrollToPosition(position)
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
                    MyAlertDialog(this).showAlertWithOKAndCancel(
                        "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
                        MyAlertDialog.MyDialogCallback(onRight = {
                            (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                                .navigation() as? IRealNameService)?.checkRealHead { e ->
                                if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                                    MyAlertDialog(this, false).showAlertWithOKAndCancel(
                                        e.busiMessage.toString(),
                                        title = "修改提示",
                                        okText = "修改头像",
                                        noText = "取消",
                                        callback = MyAlertDialog.MyDialogCallback(onRight = {
                                            RNPageActivity.start(
                                                this,
                                                RnConstant.EDIT_MINE_HOMEPAGE
                                            )
                                        })
                                    )

                                }
                            }
                        }), "真人照片未认证", okText = "去认证", noText = "取消"
                    )
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
        //重新获取数据
        if (mHomePageViewModel.mineHomePage) {
            mHomePageViewModel.homeInfo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.destroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            resetView(intent)
        }
    }
}

