package com.julun.huanque.core.ui.homepage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseActivity
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.manager.audio.AudioPlayerManager
import com.julun.huanque.common.manager.audio.MediaPlayFunctionListener
import com.julun.huanque.common.manager.audio.MediaPlayInfoListener
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.bgabanner.BGABanner
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.HomePagePicListAdapter
import com.julun.huanque.core.manager.AliPlayerManager
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.main.bird.LeYuanBirdActivity
import com.julun.huanque.core.viewmodel.HomePageViewModel
import com.julun.rnlib.RNPageActivity
import com.julun.rnlib.RnConstant
import io.rong.imlib.model.Conversation
import kotlinx.android.synthetic.main.act_home_page.*
import kotlinx.android.synthetic.main.act_home_page.stv_medal
import kotlinx.android.synthetic.main.act_home_page.tv_id
import kotlinx.android.synthetic.main.act_home_page.tv_location
import kotlinx.android.synthetic.main.act_home_page.tv_nickname
import kotlinx.android.synthetic.main.act_home_page.tv_private_chat
import kotlinx.android.synthetic.main.act_home_page.tv_sex
import kotlinx.android.synthetic.main.act_home_page.tv_tag
import kotlinx.android.synthetic.main.fragment_user_card.*
import org.jetbrains.anko.*
import kotlin.math.ceil

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

    private val mPicAdapter = HomePagePicListAdapter()

    //内容区域宽度
    private val innerWidth = ScreenUtils.getScreenWidth() - 2 * dp2px(15)

    //更多操作弹窗
    private var mHomePageActionFragment: HomePageActionFragment? = null

    //主页评论弹窗
    private var mHomePageEvaluateFragment: HomePageEvaluateFragment? = null

    //顶部开始渐变的位置
    private var mStartChange = 0

    //顶部多长时间变化完成
    private val mChangeDistance = 100

    //亲密知己弹窗
    private val mIntimacyFragment: IntimacyFragment by lazy { IntimacyFragment() }

    //座驾详情弹窗
    private val mCarDetailFragment: CarDetailFragment by lazy { CarDetailFragment() }

    private val audioPlayerManager: AudioPlayerManager by lazy { AudioPlayerManager(this) }

    override fun getLayoutId() = R.layout.act_home_page

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        val statusHeight = StatusBarUtil.getStatusBarHeight(this)
        mStartChange = ScreenUtils.getScreenWidth() * 308 / 375 - dp2px(44) - statusHeight

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        StatusBarUtil.setColor(this, GlobalUtils.formatColor("#00FFFFFF"))

        val shaderParams = view_shader.layoutParams
        shaderParams.height = statusHeight
        view_shader.layoutParams = shaderParams

        initViewModel()
        val barHeight = StatusBarUtil.getStatusBarHeight(this)
        val params = view_top.layoutParams as? ConstraintLayout.LayoutParams
        params?.topMargin = barHeight
        view_top.layoutParams = params
        val userID = intent?.getLongExtra(ParamConstant.UserId, 0) ?: 0
        mHomePageViewModel.targetUserId = userID

        //半秒回调一次
        audioPlayerManager.setSleep(500)
        audioPlayerManager.setMediaPlayFunctionListener(object : MediaPlayFunctionListener {
            override fun prepared() {
                logger.info("prepared")
            }

            override fun start() {
                logger.info("start 总长=${audioPlayerManager.getDuration()}")
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                //不使用实际的值
//                currentPlayHomeRecomItem?.introduceVoiceLength = (audioPlayerManager.getDuration() / 1000)+1
//                AliplayerManager.soundOff()
                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
            }

            override fun resume() {
                logger.info("resume")
                AliPlayerManager.soundOff()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_play_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                sdv_voice_state.setPadding(dp2px(5), dp2px(6), dp2px(5), dp2px(6))
                ImageUtils.loadGifImageLocal(sdv_voice_state, R.mipmap.voice_home_page_playing)
            }

            override fun pause() {
                logger.info("pause")
                AliPlayerManager.soundOn()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                val padding = dp2px(0)
                sdv_voice_state.setPadding(padding, padding, padding, padding)
                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
            }

            override fun stop() {
                logger.info("stop")
                AliPlayerManager.soundOn()
//                val drawable = GlobalUtils.getDrawable(R.mipmap.icon_pause_home_page)
//                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
//                tv_time.setCompoundDrawables(drawable, null, null, null)
                val padding = dp2px(0)
                sdv_voice_state.setPadding(padding, padding, padding, padding)
                ImageUtils.loadImageLocal(sdv_voice_state, R.mipmap.icon_pause_home_page)
            }


        })
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
        initRecyclerView()
        mHomePageViewModel.homeInfo()
    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
        //关注
        view_attention_home_page.onClickNew {
            if (mHomePageViewModel.followStatus.value == BusiConstant.False) {
                //未关注状态   关注
                mHomePageViewModel.follow()
            } else {
                //关注状态  取消关注
                MyAlertDialog(this).showAlertWithOKAndCancel(
                    "确定不再关注Ta？",
                    MyAlertDialog.MyDialogCallback(onRight = {
                        mHomePageViewModel.unFollow()
                    })
                )
            }
        }

        tv_id.onClickNew {
            //复制
            GlobalUtils.copyToSharePlate(this, "${mHomePageViewModel.targetUserId}", "已复制ID")
        }

        iv_more.onClickNew {
            //更多
            mHomePageActionFragment = mHomePageActionFragment ?: HomePageActionFragment()
            mHomePageActionFragment?.show(supportFragmentManager, "HomePageActionFragment")
        }

        iv_more_black.onClickNew {
            //更多操作
            iv_more.performClick()
        }

        iv_close.onClickNew {
            finish()
        }
        iv_close_black.onClickNew {
            finish()
        }


        nested_scroll_view.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val changeEnableDistance = scrollY - mStartChange

            val color: Int = when {
                changeEnableDistance <= 0 -> {
                    tv_user_name.alpha = 0f
                    iv_more.alpha = 1f
                    iv_more_black.alpha = 0f
                    iv_close.alpha = 1f
                    iv_close_black.alpha = 0f
                    GlobalUtils.formatColor("#00FFFFFF")
//                    view_top.alpha = 0f
                }
                changeEnableDistance >= mChangeDistance -> {
//                    view_top.alpha = 1f
                    tv_user_name.alpha = 1f
                    iv_more.alpha = 0f
                    iv_more_black.alpha = 1f
                    iv_close.alpha = 0f
                    iv_close_black.alpha = 1f
                    GlobalUtils.formatColor("#FFFFFFFF")
                }
                else -> {
                    val alpha = changeEnableDistance / mChangeDistance.toFloat()
                    iv_close.alpha = (1 - alpha)
                    iv_close_black.alpha = alpha
                    iv_more.alpha = (1 - alpha)
                    iv_more_black.alpha = alpha
                    tv_user_name.alpha = alpha

                    GlobalUtils.getColorWithAlpha(alpha, Color.WHITE)
                }
            }
            view_top.backgroundColor = color
            StatusBarUtil.setColor(this, color)
        }

        //播放音效
        view_voice.onClickNew { }

        //拨打电话
        tv_voice.onClickNew {
            checkAudioPermission()
        }

        tv_sendgift.onClickNew {
            //送礼
            val userId = mHomePageViewModel.targetUserId
            val bundle = Bundle()
            bundle.putLong(ParamConstant.TARGET_USER_ID, userId)
            val basicBean = mHomePageViewModel.homeInfoBean.value
            if (basicBean != null) {
                bundle.putString(ParamConstant.NICKNAME, basicBean.nickname)
                bundle.putString(ParamConstant.HeaderPic, basicBean.headPic)
            }
            bundle.putString(ParamConstant.OPERATION, OperationType.OPEN_GIFT)
            ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY)
                .with(bundle)
                .navigation(this)
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


        tv_evaluate.onClickNew {
            //评价
            val intimate = mHomePageViewModel.homeInfoBean.value?.intimate
            if (intimate == BusiConstant.True) {
                //密友
                mHomePageViewModel.getEvaluateList()
            } else {
                ToastUtils.show("成为密友才能评价哦")
            }
        }
        con_live.onClickNew {
            //跳转直播间
        }

        sdv_intim_border.onClickNew {
            val bean = mHomePageViewModel.homeInfoBean.value?.closeConfidant ?: return@onClickNew
            if (bean.userId != 0L) {
                mHomePageViewModel.closeConfidantRank()
            }
//            mIntimacyFragment.show(supportFragmentManager, "IntimacyFragment")
        }
        tv_time.onClickNew {
            //播放音效
            if (audioPlayerManager.musicType == -1 || audioPlayerManager.mediaPlayer == null) {
                //未设置音频地址
                audioPlayerManager.setNetPath(StringHelper.getOssAudioUrl(mHomePageViewModel.homeInfoBean.value?.voice?.voiceUrl ?: ""))
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
        tv_like.onClickNew {
            //点赞语音
            if (mHomePageViewModel.homeInfoBean.value?.voice?.like != BusiConstant.True) {
                mHomePageViewModel.voicePraise()
            } else {
                ToastUtils.show("你已经赞过了哦")
            }
        }

        sdv_vehicle.onClickNew {
            //显示座驾
            val carInfo = mHomePageViewModel.homeInfoBean.value?.carInfo ?: return@onClickNew
            if (carInfo.dynamicUrl.isEmpty()) {
                //座驾动画为空，跳转我的座驾页面
                RNPageActivity.start(this, RnConstant.MY_CAR_PAGE)
            } else {
                //座驾动画不为空，显示动画弹窗
                mCarDetailFragment.show(supportFragmentManager, "CarDetailFragment")
            }
        }
        tv_vehicle.onClickNew {
            sdv_vehicle.performClick()
        }

        con_live.onClickNew {
            //跳转直播间
            val programId = mHomePageViewModel.homeInfoBean.value?.programInfo?.programId ?: return@onClickNew
            PlayerActivity.start(this, programId, PlayerFrom.UserHome)
        }

        con_intim.onClickNew {
            //亲密榜
            val intimList = mHomePageViewModel.homeInfoBean.value?.closeConfidantRank?.rankList
            if (intimList?.isNotEmpty() == true) {
                mHomePageViewModel.closeConfidantRank()
            } else {
                //跳转私信
                val bundle = Bundle()
                val homeBean = mHomePageViewModel.homeInfoBean.value
                bundle.putLong(ParamConstant.TARGET_USER_ID, mHomePageViewModel.targetUserId)
                bundle.putString(ParamConstant.NICKNAME, homeBean?.nickname ?: "")
                bundle.putBoolean(ParamConstant.FROM, false)
                bundle.putString(ParamConstant.HeaderPic, homeBean?.headPic)
                ARouter.getInstance().build(ARouterConstant.PRIVATE_CONVERSATION_ACTIVITY).with(bundle).navigation()
            }
        }
        view_living.onClickNew {
            //跳转直播间
            val programId = mHomePageViewModel.homeInfoBean.value?.programInfo?.programId ?: return@onClickNew
            PlayerActivity.start(this, programId, PlayerFrom.UserHome)
        }
        tv_black_status.onClickNew {
            //屏蔽事件
        }
        ll_bird.onClickNew {
            //跳转养鹊乐园
            val intent = Intent(this, LeYuanBirdActivity::class.java)
            if (ForceUtils.activityMatch(intent)) {
                startActivity(intent)
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        recyclerView_piclist.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
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
                    }
                    NetStateType.NETWORK_ERROR -> {
                        state_pager_view.showError()
                    }
                    NetStateType.LOADING -> {
                        state_pager_view.showLoading()
                    }


                }
            }
        })
        mHomePageViewModel.homeInfoBean.observe(this, Observer {
            showViewByData(it ?: return@Observer)
        })

        mHomePageViewModel.praiseSuccessState.observe(this, Observer {
            if (it == BusiConstant.True) {
                //点赞成功
                showPraise(mHomePageViewModel.homeInfoBean.value?.voice ?: return@Observer)
            }
        })

        mHomePageViewModel.closeListData.observe(this, Observer {
            if (it != null) {
                //显示亲密榜单
                mIntimacyFragment.show(supportFragmentManager, "IntimacyFragment")
            }
        })

        mHomePageViewModel.followStatus.observe(this, Observer {
            showAttentionState(it ?: return@Observer)
        })
        mHomePageViewModel.blackStatus.observe(this, Observer {
            val bean =
                if (it == BusiConstant.True && mHomePageViewModel.homeInfoBean.value?.programInfo?.living != BusiConstant.True) {
                    //拉黑状态
                    tv_black_status.show()
                } else {
                    tv_black_status.hide()
                }
        })

        mHomePageViewModel.actionData.observe(this, Observer {
            if (it != null) {
                when (it) {
                    HomePageViewModel.ACTION_BLACK -> {
                        //拉黑

                        if (mHomePageViewModel.blackStatus.value == BusiConstant.True) {
                            //解除黑名单
                            MyAlertDialog(
                                this
                            ).showAlertWithOKAndCancel(
                                "将对方移除黑名单",
                                MyAlertDialog.MyDialogCallback(onRight = {
                                    //拉黑
                                    if (mHomePageViewModel.blackStatus.value == BusiConstant.True) {
                                        mHomePageViewModel?.recover()
                                    }

                                }), "移除黑名单", "确定", "取消"
                            )
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
                showEvaluate(it ?: return@Observer)
            }
        })
    }

    /**
     * 显示关注状态
     */
    private fun showAttentionState(status: String) {
        when (status) {
            FollowStatus.False -> {
                //未关注
                tv_attention_home_page.isSelected = true
                view_attention_home_page.isSelected = true
                tv_attention_home_page.text = "关注"
            }
            FollowStatus.True -> {
                //已关注
                tv_attention_home_page.isSelected = false
                view_attention_home_page.isSelected = false
                tv_attention_home_page.text = "已关注"
            }
            FollowStatus.Mutual -> {
                //互相关注
                tv_attention_home_page.isSelected = false
                view_attention_home_page.isSelected = false
                tv_attention_home_page.text = "相互关注"
            }
        }
    }

    /**
     * 显示页面
     */
    private fun showViewByData(bean: HomePageInfo) {
        tv_user_name.text = bean.nickname
        val picList = mutableListOf<HomePagePicBean>()
        picList.add(HomePagePicBean(bean.headPic, bean.headRealPeople, BusiConstant.True))
        bean.picList.forEach {
            picList.add(HomePagePicBean(it))
        }
        showBanner(picList)
        mPicAdapter.setList(picList)

        //亲密知己数据
        if (bean.showCloseConfidant == BusiConstant.True) {
            //显示亲密知己
            sdv_intim_border.show()
            sdv_intim_header.show()
            val intimBean = bean.closeConfidant
            if (intimBean.userId > 0) {
                //显示亲密知己数据
                sdv_intim_border.imageResource = R.mipmap.pic_intim
                sdv_intim_header.loadImage(StringHelper.getOssImgUrl(intimBean.headPic), 47f, 47f)
            } else {
                //显示默认状态
                sdv_intim_border.imageResource = R.mipmap.pic_no_intim
            }
        } else {
            //不显示亲密知己
            sdv_intim_border.hide()
            sdv_intim_header.hide()
        }
//        sdv_header.loadImage(bean.headPic, 70f, 70f)
        if (bean.voice.voiceStatus == VoiceBean.Pass) {
            //审核通过 显示语音签名
            showPraise(bean.voice)
            tv_time.text = "${bean.voice.length}s"
            view_voice.show()
            tv_time.show()
            sdv_voice_state.show()
        } else {
            //不显示语音签名
            view_voice.hide()
            tv_time.hide()
            view_voice.hide()
            view_voice_divider.hide()
            tv_like.hide()
            sdv_voice_state.hide()
        }

        //显示座驾相关视图
        val carInfo = bean.carInfo
        sdv_vehicle.loadImage(StringHelper.getOssImgUrl(carInfo.carPic), 110f, 74f)
        if (carInfo.dynamicUrl.isEmpty()) {
            tv_vehicle.text = "${carInfo.showMsg}>"
        } else {
            tv_vehicle.text = "${carInfo.carName}>"
        }


//        //实人认证标识
//        if (bean.headRealPeople == BusiConstant.True) {
//            //实人认证
//            sdv_real.show()
//            sdv_real.loadImage(bean.authMark, 53f, 16f)
//        } else {
//            sdv_real.hide()
//        }

        tv_nickname.text = bean.nickname
        val sex = bean.sex
        var sexDrawable: Drawable? = null
        //性别
        when (sex) {
            Sex.MALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_male
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_male)
                tv_sex.textColor = Color.parseColor("#58CEFF")
            }
            Sex.FEMALE -> {
                tv_sex.backgroundResource = R.drawable.bg_shape_mkf_sex_female
                sexDrawable = GlobalUtils.getDrawable(R.mipmap.icon_sex_female)
                tv_sex.textColor = Color.parseColor("#FF9BC5")
            }
            else -> sexDrawable = null
        }
        if (sexDrawable != null) {
            sexDrawable.setBounds(0, 0, sexDrawable.minimumWidth, sexDrawable.minimumHeight)
            tv_sex.setCompoundDrawables(sexDrawable, null, null, null)
        } else {
            tv_sex.setCompoundDrawables(null, null, null, null)
        }
        //性别+星座
        val sexContent = "${bean.age} | ${bean.constellation}"
        val sexStringSpannable = SpannableStringBuilder(sexContent)
        val sexStartIndex = "${bean.age} ".length
        val lightColor = if (sex == Sex.FEMALE) {
            GlobalUtils.formatColor("#FCE5EB")
        } else {
            GlobalUtils.formatColor("#58CEFF")
        }
        sexStringSpannable.setSpan(
            ForegroundColorSpan(lightColor),
            sexStartIndex,
            sexStartIndex + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        tv_sex.text = sexStringSpannable

        val city = bean.currentCity
        if (city.isEmpty()) {
            tv_location.hide()
        } else {
            tv_location.show()
            tv_location.text = city
        }

//        ImageUtils.loadImageLocal(sdv_caifu_level, ImageHelper.getUserLevelImg(bean.userLevel))
        val royalLevel = bean.royalLevel
//        if (royalLevel > 0) {
////            sdv_guizu_level.show()
//            ImageUtils.loadImageLocal(
//                sdv_guizu_level,
//                ImageHelper.getRoyalLevelImgLong(bean.royalLevel)
//            )
//        } else {
//            sdv_guizu_level.hide()
//        }

        val anchorLevel = bean.anchorLevel
//        if (anchorLevel > 0) {
////            sdv_anchor_level.show()
//            ImageUtils.loadImageLocal(
//                sdv_anchor_level,
//                ImageHelper.getAnchorLevelResId(anchorLevel)
//            )
//        } else {
//            sdv_anchor_level.hide()
//        }

        //基础信息
        tv_id.text = "欢鹊ID：${bean.userId}"

//        val height = bean.height
//        if (height != 0) {
//            tv_height.text = "${bean.height}cm"
//        } else {
//            tv_height.text = "-"
//        }

//        val weight = bean.weight
//        if (weight != 0) {
//            tv_weight.text = "${bean.weight}kg"
//        } else {
//            tv_weight.text = "-"
//        }
        if (bean.weight == 0 || bean.height == 0) {
            tv_weight_height.hide()
        } else {
            if (bean.weight != 0 && bean.height != 0) {
                val weightContent = "${bean.height}cm | ${bean.weight}kg"
                val weightStringSpannable = SpannableStringBuilder(weightContent)
                val weightStartIndex = "${bean.height}cm ".length
                weightStringSpannable.setSpan(
                    ForegroundColorSpan(GlobalUtils.formatColor("#DAE8F3")),
                    weightStartIndex,
                    weightStartIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                tv_weight_height.text = weightStringSpannable

            } else if (bean.weight != 0 && bean.height == 0) {
                tv_weight_height.text = "${bean.weight}kg"
            } else {
                tv_weight_height.text = "${bean.height}cm"
            }

        }


//        val profession = bean.jobName
//        if (profession.isNotEmpty()) {
//            tv_profession.text = bean.jobName
//        } else {
//            tv_profession.text = "-"
//        }


        tv_sign_home.text = bean.mySign

        //显示关注，粉丝，访客数据
        val tabList = bean.userDataTabList
        tabList.forEach {
            when (it.userDataTabType) {
                "Follow" -> {
                    //关注
                    tv_attetnion_number.text = "关注 ${it.count}"
                }
                "Fan" -> {
                    //粉丝
                    tv_fans_number.text = "粉丝 ${it.count}"
                }
                "Visit" -> {
                    //访客
                    tv_visitor_number.text = "访客 ${it.count}"
                }
                else -> {
                }
            }
        }

        //直播相关
        val playProgram = bean.playProgram
        if (playProgram.programId != 0L) {
            //主播
            con_live.show()
            view_live.show()
            sdv_cover.loadImage(playProgram.programCover, 80f, 80f)
            if (playProgram.living == BusiConstant.True) {
                //开播中
                tv_living.show()
                sdv_living.show()
                ImageUtils.loadGifImageLocal(sdv_living, R.mipmap.living_home_page_player)
                tv_watch_count.text = "${playProgram.onlineUserNum}人围观中"
                tv_living.text = "直播中"
            } else {
                //未开播
                tv_watch_count.text = "期待Ta的下一场直播"
                tv_watch_count.textColor = GlobalUtils.getColor(R.color.black_999)
                tv_floow_watch.hide()
                sdv_living.hide()
                tv_living.text = "暂未开播"
            }
        } else {
            con_live.hide()
            view_live.hide()
        }

        //养鹊相关
        val playParadise = bean.playParadise
        if (playParadise.magpieList.isNotEmpty()) {
            //有小鹊数据
            ll_bird.show()
            view_bird.show()
            tv_fly_money.text = playParadise.showText
            playParadise.magpieList.forEach {
                val view = LayoutInflater.from(this).inflate(R.layout.view_bird_home_page, null)
                view.findViewById<SimpleDraweeView>(R.id.sdv_bird).loadImage(StringHelper.getOssImgUrl(it.upgradeIcon), 40f, 37f)
                view.findViewById<TextView>(R.id.tv_level).text = "${it.upgradeLevel}级"
                ll_bird.addView(view)
            }
        } else {
            //无小鹊数据
            ll_bird.hide()
            view_bird.hide()
        }

        //亲密榜相关
        if (bean.sex == Sex.FEMALE) {
            con_intim.show()
            view_intim.show()
            val closeConfidantRank = bean.closeConfidantRank
            val viewList = mutableListOf<SimpleDraweeView>()
            val count = closeConfidantRank.rankList.size
            if (count > 0) {
                //有亲密好友，添加视图
                viewList.add(sdv_third)
                if (count > 1) {
                    viewList.add(0, sdv_second)
                }
                if (count > 2) {
                    viewList.add(0, sdv_one)
                }

                closeConfidantRank.rankList.forEachIndexed { index, s ->
                    if (ForceUtils.isIndexNotOutOfBounds(index, viewList)) {
                        val tempView = viewList[index]
                        tempView.show()
                        tempView.loadImage(StringHelper.getOssImgUrl(s), 40f, 40f)
                    }
                }
                tv_intim.text = "本周亲密榜（${closeConfidantRank.rankNum}人）"
            } else {
                //无亲密好友，显示空布局
                tv_no_user.show()
                tv_intim.text = "本周亲密榜（0人）"
            }

        } else {
            con_intim.hide()
            view_intim.hide()
        }

        if (playProgram.programId == 0L && playParadise.magpieList.isEmpty() && bean.sex == Sex.MALE) {
            tv_play.hide()
        } else {
            tv_play.show()
        }



        showGuanfang(bean.iconList)
        showTags(bean.characterTag)
        showEvaluate(bean.appraiseList)
        showMap(bean.cityList, bean.myCityList)

        //显示底部视图
        val liveStatus = bean.programInfo?.living ?: ""
        if (liveStatus == BusiConstant.True) {
            //开播状态
            tv_living_bottom.show()
            sdv_living_bottom.show()
            ImageUtils.loadGifImageLocal(sdv_living_bottom, R.mipmap.living_home_page_bottom)
            view_living.show()
            view_private_chat.hide()
            tv_private_chat.hide()
        } else {
            //未处于开播状态
            tv_living_bottom.hide()
            sdv_living_bottom.hide()
            view_living.hide()
            view_private_chat.show()
            tv_private_chat.show()
        }
    }

    /**
     * 显示点赞视图
     */
    private fun showPraise(bean: VoiceBean) {
        tv_like.isSelected = bean.like == BusiConstant.True
        tv_like.text = "${bean.likeCount}"

    }


    private val bannerAdapter by lazy {
        BGABanner.Adapter<View, HomePagePicBean> { _, itemView, model, _ ->
            val pic = itemView?.findViewById<SimpleDraweeView>(R.id.sdv) ?: return@Adapter

            ImageUtils.loadImage(pic, "${model?.pic}")
            val icWater = itemView?.findViewById<ImageView>(R.id.iv_water) ?: return@Adapter
            if (model?.realPic == BusiConstant.True) {
                icWater.show()
            } else {
                icWater.hide()
            }
        }
    }

    private val bannerItemClick by lazy {
        BGABanner.Delegate<SimpleDraweeView, HomePagePicBean> { _, _, model, posisiton ->
//            val bean = mHomePageViewModel.homeInfoBean.value ?: return@Delegate
//            val imageList = bean.picList.apply { add(bean.headPic) }
//            val picList = mutableListOf<String>()
//            imageList.forEach { picList.add(StringHelper.getOssImgUrl(it)) }
//            ImageActivity.start(this, posisiton, picList)
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

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
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
    private fun showGuanfang(badges: MutableList<String>) {
        if (badges.isEmpty()) {
            //没有勋章，隐藏
            stv_medal.hide()
        } else {
            val list = arrayListOf<TIBean>()
            stv_medal.show()
            badges.forEach {
                if (!TextUtils.isEmpty(it)) {
                    val image = TIBean()
                    image.type = 1
                    image.url = StringHelper.getOssImgUrl(it)
                    image.height = dp2px(18)
                    image.width = dp2px(66)
                    list.add(image)
                }
            }

            val textBean = ImageUtils.renderTextAndImage(list)
            stv_medal.renderBaseText(textBean ?: return)
        }
    }


    /**
     * 显示标签数据
     */
    private fun showTags(characterTag: CharacterTag) {
        val tagList = characterTag.characterTagList
        val favoriteTagList = characterTag.favoriteTagList
        val characterList = mutableListOf<String>()
        characterList.addAll(tagList)
        favoriteTagList.forEach {
            if (!characterList.contains(it)) {
                characterList.add(it)
            }
        }
        if (characterList.isEmpty()) {
            //没有标签数据
            linefeed_ll_tag.hide()
            tv_tag.hide()
        } else {
            linefeed_ll_tag.show()
            tv_tag.show()
            //行数
            var line = 1
            var currentWidth = 0
            //是否是当前行的第一个元素
            characterList.forEach { tag ->
                val tv = TextView(this)
                    .apply {
                        text = tag
//                        if (favoriteTagList.contains(tag)) {
//                            backgroundResource = R.drawable.bg_tag_sel
//                            textColor = GlobalUtils.getColor(R.color.black_333)
//                        } else {
                        backgroundResource = R.drawable.bg_tag_normal
                        textColor = GlobalUtils.getColor(R.color.black_666)
//                        }
                        textSize = 14f
                        gravity = Gravity.CENTER
                    }
                tv.setPadding(dp2px(15), 0, dp2px(15), 0)
                val params =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp2px(30f))
                params.rightMargin = dp2px(15f)
                params.topMargin = dp2px(12f)

                val needWidth = ScreenUtils.getViewRealWidth(tv)
                if (currentWidth + needWidth > innerWidth) {
                    //换行
                    line++
                    currentWidth = 0
                }
                currentWidth += needWidth + dp2px(15)
                linefeed_ll_tag.addView(tv, params)
            }

            val llParams = linefeed_ll_tag.layoutParams
            llParams.height = dp2px(42) * line
            linefeed_ll_tag.layoutParams = llParams
        }

    }


    /**
     * 显示评价
     */
    private fun showEvaluate(appraiseList: MutableList<AppraiseBean>) {
        linefeed_ll_evaluate.removeAllViews()
        if (appraiseList.isEmpty()) {
            tv_empty_evaluate.show()
        } else {
            tv_empty_evaluate.hide()
        }
        //添加元素
        appraiseList.add(AppraiseBean(0, "评价Ta +"))
        //行数
        var line = 1
        var currentWidth = 0
        appraiseList.forEach { appraise ->
            val num = appraise.agreeNum
            val tv = TextView(this)
                .apply {

                    if (num == 0) {
                        //评价元素
                        text = appraise.content
                        backgroundResource = R.drawable.bg_enable
                        textColor = GlobalUtils.getColor(R.color.black_333)
                    } else {
                        //普通元素
                        text = "${appraise.content} ${appraise.agreeNum}"
                        backgroundResource = R.drawable.bg_appraise
                        textColor = GlobalUtils.getColor(R.color.black_666)
                    }
                    textSize = 14f
                    gravity = Gravity.CENTER
                }
            tv.setPadding(dp2px(15), 0, dp2px(15), 0)
            val params =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp2px(30f))
            params.rightMargin = dp2px(15f)
            params.topMargin = dp2px(15f)

            val needWidth = ScreenUtils.getViewRealWidth(tv)
            if (currentWidth + needWidth > innerWidth) {
                //换行
                line++
                currentWidth = 0
            }

            currentWidth += needWidth + dp2px(15)
            linefeed_ll_evaluate.addView(tv, params)
            if (num == 0) {
                tv.onClickNew { tv_evaluate.performClick() }
            }


        }
        val llParams = linefeed_ll_evaluate.layoutParams
        llParams.height = dp2px(45) * line
        linefeed_ll_evaluate.layoutParams = llParams
    }


    /**
     * 显示地图数据
     */
    private fun showMap(cityList: MutableList<CityBean>, myCityList: MutableList<CityBean>) {
//        if (cityList.size <= 0) {
//            //没有足迹，隐藏
//            tv_footprint.hide()
//            hsv.hide()
//        } else {
        tv_footprint.show()
        hsv.show()
        if (false) {
            //我的主页使用
            //添加一个待添加的城市
            cityList.add(CityBean())
            val count = ceil((cityList.size) / 5.toDouble()).toInt()
            val lineBitmap = BitmapFactory.decodeResource(resources, R.mipmap.pic_map_curve)
            (0 until count).forEach { index ->
                val singleView = LayoutInflater.from(this).inflate(R.layout.view_single_map, null)
                val llParams =
                    LinearLayout.LayoutParams(innerWidth, ViewGroup.LayoutParams.MATCH_PARENT)
                ll_map.addView(singleView, llParams)

                showMapView(
                    index,
                    singleView,
                    cityList,
                    myCityList,
                    lineBitmap
                )
            }
        } else {
            //他人主页使用
            val otherMapView = LayoutInflater.from(this).inflate(R.layout.view_single_map_other, null)
            val llParams =
                LinearLayout.LayoutParams(innerWidth, ViewGroup.LayoutParams.MATCH_PARENT)
            ll_map.addView(otherMapView, llParams)

            //计算视图位置
            val iv_owner = otherMapView.findViewById<ImageView>(R.id.iv_owner) ?: return
            val iv_visitor = otherMapView.findViewById<ImageView>(R.id.iv_visitor) ?: return

            //计算主页对象的位置
            val ownerTopMargin = innerWidth * 20 / 345
            val ownerLeftMargin = innerWidth * 65 / 345
            val ownerParams = iv_owner.layoutParams as? ConstraintLayout.LayoutParams
            ownerParams?.topMargin = ownerTopMargin
            ownerParams?.leftMargin = ownerLeftMargin
            iv_owner.layoutParams = ownerParams


            //计算观看者的位置
            val visitorTopMargin = innerWidth * 21 / 345
            val visitorRightMargin = innerWidth * 82 / 345
            val visitorParams = iv_visitor.layoutParams as? ConstraintLayout.LayoutParams
            visitorParams?.topMargin = visitorTopMargin
            visitorParams?.rightMargin = visitorRightMargin
            iv_visitor.layoutParams = visitorParams

            val homeCityInfo = mHomePageViewModel.homeInfoBean.value?.homeCity ?: return
            //设置头像
            otherMapView.findViewById<SimpleDraweeView>(R.id.sdv_owner_header)
                ?.loadImage("${StringHelper.getOssImgUrl(homeCityInfo.homeHeadPic)}${BusiConstant.OSS_160}", 35f, 35f)
            val sdv_visitor_header = otherMapView.findViewById<SimpleDraweeView>(R.id.sdv_visitor_header)
            sdv_visitor_header?.loadImage("${StringHelper.getOssImgUrl(homeCityInfo.curryHeadPic)}${BusiConstant.OSS_160}", 25f, 25f)
            sdv_visitor_header?.onClickNew {
                RNPageActivity.start(this, RnConstant.MINE_HOMEPAGE)
            }

            //设置位置和距离
            val homeCityName = homeCityInfo.homeCityName
            val currentCityName = homeCityInfo.curryCityName


            otherMapView.findViewById<TextView>(R.id.tv_owner_cityname)?.text = if (homeCityName.isEmpty()) {
                "未知星球"
            } else {
                homeCityName
            }

            otherMapView.findViewById<TextView>(R.id.tv_visitor_cityname)?.text = if (currentCityName.isEmpty()) {
                "未知星球"
            } else {
                currentCityName
            }

            otherMapView.findViewById<TextView>(R.id.tv_distance)?.text = if (homeCityName.isEmpty() || currentCityName.isEmpty()) {
                "相距?km"
            } else if (currentCityName == homeCityName) {
                "转角遇到Ta"
            } else {
                "${homeCityInfo.distance}km"
            }
        }

    }

    /**
     * 显示地图数据
     */
    private fun showMapView(
        index: Int,
        singleView: View,
        cityList: MutableList<CityBean>,
        myCityList: MutableList<CityBean>,
        lineBitmap: Bitmap
    ) {
        //总共的页数
        val count = ceil((cityList.size) / 5.toDouble()).toInt()
        val myCityIdList = mutableListOf<Long>()
        myCityList.forEach {
            myCityIdList.add(it.cityId)
        }

        val topMargin_1 = innerWidth * 58 / 345
        val topMargin_2 = innerWidth * 28 / 345
        val topMargin_3 = innerWidth * 62 / 345
        val topMargin_4 = innerWidth * 38 / 345
        val topMargin_5 = innerWidth * 69 / 345

        val iv_point_1 = singleView.findViewById<ImageView>(R.id.iv_point_1) ?: return
        val iv_point_2 = singleView.findViewById<ImageView>(R.id.iv_point_2) ?: return
        val iv_point_3 = singleView.findViewById<ImageView>(R.id.iv_point_3) ?: return
        val iv_point_4 = singleView.findViewById<ImageView>(R.id.iv_point_4) ?: return
        val iv_point_5 = singleView.findViewById<ImageView>(R.id.iv_point_5) ?: return

        val tvCity_name_1 = singleView.findViewById<TextView>(R.id.tv_city_name_1) ?: return
        val tvCity_name_2 = singleView.findViewById<TextView>(R.id.tv_city_name_2) ?: return
        val tvCity_name_3 = singleView.findViewById<TextView>(R.id.tv_city_name_3) ?: return
        val tvCity_name_4 = singleView.findViewById<TextView>(R.id.tv_city_name_4) ?: return
        val tvCity_name_5 = singleView.findViewById<TextView>(R.id.tv_city_name_5) ?: return

        //线
        val view_line = singleView.findViewById<ImageView>(R.id.view_line) ?: return

        val pointList = arrayListOf<ImageView>(
            iv_point_1,
            iv_point_2,
            iv_point_3,
            iv_point_4,
            iv_point_5
        )
        val cityNameList = arrayListOf<TextView>(
            tvCity_name_1,
            tvCity_name_2,
            tvCity_name_3,
            tvCity_name_4,
            tvCity_name_5
        )

        setMapMargin(iv_point_1, topMargin_1)
        setMapMargin(iv_point_2, topMargin_2)
        setMapMargin(iv_point_3, topMargin_3)
        setMapMargin(iv_point_4, topMargin_4)
        setMapMargin(iv_point_5, topMargin_5)

        var pointCount = 0

        //显示数据
        (0 until 5).forEach {
            val cityIndex = index * 5 + it
            if (ForceUtils.isIndexNotOutOfBounds(cityIndex, cityList)) {
                pointCount = it + 1
                val tempCity = cityList[cityIndex]
                val cityId = tempCity.cityId
                val pointResource: Int
                val cityName: String
                when {
                    cityId == 0L -> {
                        //待添加城市
                        pointResource = R.mipmap.circle_map_blue
                        cityName = "待添加"
                    }
                    myCityIdList.contains(tempCity.cityId) -> {
                        //共有城市
                        pointResource = R.mipmap.circle_map_red
                        cityName = tempCity.cityName
                    }
                    else -> {
                        //非共有城市
                        pointResource = R.mipmap.circle_map_yellow
                        cityName = tempCity.cityName
                    }
                }
                val ivPoint = pointList[it]
                val tvCityName = cityNameList[it]
                ivPoint.show()
                tvCityName.show()

                ivPoint.imageResource = pointResource
                tvCityName.text = cityName

                if (tempCity.cityType == CityBean.Home) {
                    //故乡
                    val tv_home_town_1 =
                        singleView.findViewById<View>(R.id.tv_home_town_1) ?: return@forEach
                    val homeParams = tv_home_town_1.layoutParams as? ConstraintLayout.LayoutParams
                    homeParams?.leftToLeft = tvCityName.id
                    homeParams?.rightToRight = tvCityName.id
                    tv_home_town_1.layoutParams = homeParams
                    tv_home_town_1.show()
                } else if (tempCity.cityType == CityBean.Resident) {
                    //常驻地
                    val iv_location =
                        singleView.findViewById<View>(R.id.iv_location) ?: return@forEach
                    val sdv_location_header =
                        singleView.findViewById<SimpleDraweeView>(R.id.sdv_location_header)
                            ?: return@forEach

                    val ivLocationParams =
                        iv_location.layoutParams as? ConstraintLayout.LayoutParams
                    ivLocationParams?.leftToLeft = ivPoint.id
                    ivLocationParams?.rightToRight = ivPoint.id
                    ivLocationParams?.bottomToBottom = ivPoint.id
                    iv_location.layoutParams = ivLocationParams
                    sdv_location_header.loadImage(
                        mHomePageViewModel.homeInfoBean.value?.headPic ?: "", 19f, 19f
                    )
                    iv_location.show()
                    sdv_location_header.show()
                }
            }
        }

        //计算线的宽度
        //是否是第一页，第一页 截断第一部分的线
        val firstPage = index == 0
        //是否是最后一页，最后一页 截断最后一部分的线
        val lastPage = index == count - 1
        //将线10等分
        val tempWidth = innerWidth / 10.toDouble()
        var tempWidthCount = (pointCount - 1) * 2
        if (!firstPage) {
            tempWidthCount++
        }
        if (!lastPage) {
            tempWidthCount++
        }

        val lineWidth = (tempWidth * tempWidthCount).toInt()
        val params = view_line.layoutParams as? ConstraintLayout.LayoutParams
        params?.width = lineWidth
        if (firstPage) {
            params?.leftMargin = tempWidth.toInt()
        } else {
            params?.leftMargin = 0
        }
        view_line.layoutParams = params
        if (tempWidthCount < 10) {
            //需要对比bitmap进行剪裁
            val bitmapWidth = lineBitmap.width
            val bitmapHeight = lineBitmap.height
            val tempBitmapWidth = bitmapWidth / 10.toDouble()
            //裁剪的起始坐标
            val start = if (firstPage) {
                tempBitmapWidth
            } else {
                0.0
            }
            //裁剪的宽度
            val cuteBitmapWidth = tempBitmapWidth * tempWidthCount
            val realBitmap = Bitmap.createBitmap(
                lineBitmap,
                start.toInt(),
                0,
                cuteBitmapWidth.toInt(),
                bitmapHeight
            )
            view_line.setImageBitmap(realBitmap)
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

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerManager.destroy()
    }

}

