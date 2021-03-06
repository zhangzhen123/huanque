package com.julun.huanque.core.ui.main.heartbeat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.alibaba.android.arouter.launcher.ARouter
import com.baidu.location.BDLocation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.base.dialog.CommonDialogFragment
import com.julun.huanque.common.base.dialog.MyAlertDialog
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.bean.beans.NearbyUserBean
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.events.CanSeeMaxCountChangeEvent
import com.julun.huanque.common.bean.events.LikeEvent
import com.julun.huanque.common.bean.events.PicChangeEvent
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.interfaces.routerservice.IRealNameService
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.*
import com.julun.huanque.common.utils.device.PhoneUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.cardlib.CardLayoutManager
import com.julun.huanque.common.widgets.cardlib.CardSetting
import com.julun.huanque.common.widgets.cardlib.CardTouchHelperCallback
import com.julun.huanque.common.widgets.cardlib.OnSwipeCardListener
import com.julun.huanque.common.widgets.cardlib.utils.ReItemTouchHelper
import com.julun.huanque.common.widgets.layoutmanager.AutoCenterLayoutManager
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.NearbyPicListAdapter
import com.julun.huanque.core.ui.homepage.EditInfoActivity
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.homepage.TagFragment
import com.julun.huanque.core.ui.tag_manager.TagUserPicsActivity
import com.julun.huanque.core.viewmodel.MainConnectViewModel
import com.julun.huanque.core.widgets.HomeCardTagView
import kotlinx.android.synthetic.main.fragment_nearby.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.imageResource
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.max
import kotlin.random.Random

class NearbyFragment : BaseLazyFragment() {
    companion object {
        fun newInstance() = NearbyFragment()
    }


    private val mViewModel: NearbyViewModel by viewModels()

    private val mMainConnectViewModel: MainConnectViewModel by activityViewModels()

    private val randomDistance: Int by lazy {
        (ScreenUtils.getScreenWidth() - dp2px(20)) / 3 - dp2px(26)
    }
    private lateinit var mReItemTouchHelper: ReItemTouchHelper
    private val list = mutableListOf<NearbyUserBean>()
    private val myTagList = mutableListOf<UserTagBean>()
    private val myLikeTagList = mutableListOf<UserTagBean>()
    override fun lazyLoadData() {
        checkPermission()
    }

    override fun getLayoutId(): Int = R.layout.fragment_nearby

    override fun isRegisterEventBus(): Boolean {
        return true
    }

    private var commonDialogFragment: CommonDialogFragment? = null

    //当前能滑动的方向集合
    private var mSwipeDirection = ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initLoadingAni()
        initViewModel()
        val setting: CardSetting = object : CardSetting() {
            override fun couldSwipeOutDirection(): Int {
                return mSwipeDirection
            }

            override fun getCardRotateDegree(): Float {
                return -8f
            }

            override fun getCardTranslateDistance(): Int {
                return 30
            }

            override fun enableHardWare(): Boolean {
                return false
            }

            //代表滑动多少比例才判定为移除
            override fun getSwipeThreshold(): Float {
                return 0.2f
            }

            override fun getCardScale(): Float {
                return 0.05f
            }

            override fun getSwipeOutAnimDuration(): Int {
                return 300
            }

            override fun isLoopCard(): Boolean {
                return false
            }

            override fun getStackDirection(): Int {
                return ReItemTouchHelper.DOWN
            }

            override fun getShowCount(): Int {
                return 2
            }
        }
        setting.setSwipeListener(object : OnSwipeCardListener<NearbyUserBean> {
            var currentDirection: Int = 0
            override fun onSwiping(
                viewHolder: RecyclerView.ViewHolder?,
                dx: Float,
                dy: Float,
                direction: Int,
                ratio: Float
            ) {
                val holder: BaseViewHolder = viewHolder as BaseViewHolder
                currentDirection = direction
                when (direction) {
                    ReItemTouchHelper.DOWN -> {
//                        Log.e("aaa", "swiping direction=down")
                        holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
                    }
                    ReItemTouchHelper.UP -> {
                        holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
//                        Log.e("aaa", "swiping direction=up")
                    }
                    ReItemTouchHelper.LEFT -> {
//                        Log.e("aaa", "swiping direction=left")
                        holder.getView<ImageView>(R.id.iv_dislike).alpha = ratio
                    }
                    ReItemTouchHelper.RIGHT -> {
                        logger.info("swiping direction=right dx=$dx dy=$dy ratio=${ratio}")
                        holder.getView<ImageView>(R.id.iv_like).alpha = ratio
                    }
                }
                stopAni()
            }

            override fun onSwipedOut(viewHolder: RecyclerView.ViewHolder?, o: NearbyUserBean, direction: Int) {
                logger.info("onSwipedOut")
                val holder: BaseViewHolder = viewHolder as BaseViewHolder
                holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f


                when (direction) {
                    ReItemTouchHelper.DOWN -> {

                    }
                    ReItemTouchHelper.UP -> {
                    }
                    ReItemTouchHelper.LEFT -> {
                        mViewModel.noFeel(o.userId)

                    }
                    ReItemTouchHelper.RIGHT -> {
                        mViewModel.like(o.userId)
                    }
                }
                startPlayAni(reset = true)
                checkToGetMore()
            }

            override fun onSwipedClear() {

            }

            override fun onSwipedOutUnable(selected: RecyclerView.ViewHolder?) {
                val holder: BaseViewHolder? = selected as? BaseViewHolder
                if (holder != null) {
                    holder.getView<ImageView>(R.id.iv_like).alpha = 0f
                    holder.getView<ImageView>(R.id.iv_dislike).alpha = 0f
                }
                when (currentDirection) {
                    ReItemTouchHelper.LEFT -> {
                        if (mViewModel.currentRemainTimes <= 0) {
                            //
                            logger.info("跳转空白页")
                            showLoading(ResponseError(0, "今日查看附近次数已经用完，去看看其他喜欢的人吧。"))
                        }
                    }
                    ReItemTouchHelper.RIGHT -> {
                        logger.info("onSwipedClear direction=RIGHT mViewModel.currentHeartTouchTimes=${mViewModel.currentHeartTouchTimes}")
                        if (mViewModel.currentRemainTimes <= 0) {
                            //
                            logger.info("右划也优先判断总次数")
                            showLoading(ResponseError(0, "今日查看附近次数已经用完，去看看其他喜欢的人吧。"))
                        } else if (mViewModel.currentHeartTouchTimes <= 0) {
                            //
                            logger.info("提示弹窗 心动次数不足")
                            val res = if (SessionUtils.getSex() == Sex.MALE) {
                                R.mipmap.bg_dialog_heart_full_male
                            } else {
                                R.mipmap.bg_dialog_heart_full_female
                            }
                            CommonDialogFragment.create(
                                dialog = commonDialogFragment, title = "心动数已达到上限",
                                content = mViewModel.heartTouchNotEnoughTips,
                                imageRes = res,
                                okText = "我知道了",
                                cancelText = "去看看我心动的人",
                                callback = CommonDialogFragment.Callback(onCancel = {
                                    ARouter.getInstance().build(ARouterConstant.HEART_BEAT_ACTIVITY).navigation()
                                })
                            ).show(requireActivity(), "CommonDialogFragment")
                        }

                    }
                }

            }

        })
        val helperCallback: CardTouchHelperCallback<*> = CardTouchHelperCallback<NearbyUserBean>(
            mRecyclerView,
            list, setting
        )
        mReItemTouchHelper = ReItemTouchHelper(helperCallback)
        val layoutManager = CardLayoutManager(mReItemTouchHelper, setting)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.adapter = cardsAdapter
        (mRecyclerView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        cardsAdapter.setOnItemChildClickListener { adapter, view, position ->
            logger.info("setOnItemChildClickListener 点击了${position}")
            val item = cardsAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener
            when (view.id) {
                R.id.ani_tag_01, R.id.ani_tag_02, R.id.ani_tag_03, R.id.ani_tag_04 -> {
                    val v = view as HomeCardTagView
                    val data = v.getCurrentData()

                    if (data != null) {
                        TagUserPicsActivity.start(requireActivity(), data.tagId, item.userId)
                    }

                }
                R.id.iv_super_like -> {
                    //todo
                    logger.info("超级喜欢")
                }
                R.id.cl_container -> {
                    val parentView = view.parent as? View ?: return@setOnItemChildClickListener
                    val tempBean = cardsAdapter.getItemOrNull(position) ?: return@setOnItemChildClickListener

//                    val shareView = parentView.findViewById<View>(R.id.card_img)
////                    val tv_user_name = parentView.findViewById<View>(R.id.tv_user_name)
//
//                    val intent = Intent(requireActivity(), HomePageActivity::class.java)
//                    val pair1: androidx.core.util.Pair<View, String> =
//                        androidx.core.util.Pair(shareView, ViewCompat.getTransitionName(shareView) ?: "")
////                    val pair2: androidx.core.util.Pair<View, String> =
////                        androidx.core.util.Pair(tv_user_name, ViewCompat.getTransitionName(tv_user_name) ?: "")
//
//                    /**
//                     * 4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
//                     */
//                    val activityOptionsCompat: ActivityOptionsCompat =
//                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), pair1)
//
//                    intent.putExtra(ParamConstant.UserId, tempBean.userId)
//                    intent.putExtra(ParamConstant.NearByBean, tempBean)
//                    startActivity(intent, activityOptionsCompat.toBundle())
                    if (tempBean.cardType == CardType.GUIDE) {
                        AppHelper.openTouch(tempBean.touchType, activity = requireActivity())
                    } else {
                        val pic = tempBean.coverPicList.getOrNull(tempBean.selectIndex) ?: ""
                        HomePageActivity.newInstance(requireActivity(), item.userId, showPic = pic)
                        reportClick(StatisticCode.EnterUserPage+StatisticCode.Nearby)
                    }

                }
                R.id.tv_bottom_tips -> {
                    if (!item.likeTagList.isEmpty()) {
                        if (item.sex != SessionUtils.getSex()) {
                            item.likeTagList.forEach {
                                if (myTagList.contains(it)) {
                                    it.mark = BooleanType.TRUE
                                } else {
                                    it.mark = BooleanType.FALSE
                                }
                            }
                        } else {
                            item.likeTagList.forEach {
                                if (myLikeTagList.contains(it)) {
                                    it.mark = BooleanType.TRUE
                                } else {
                                    it.mark = BooleanType.FALSE
                                }
                            }
                        }

                        val tagFragment = TagFragment.newInstance(
                            true,
                            item.sex == SessionUtils.getSex(),
                            item.userId == SessionUtils.getUserId(),
                            item.userId,
                            item.likeTagList
                        )
                        tagFragment.show(childFragmentManager, "tagFragment")
                    }
                }
                R.id.iv_auth_tag -> {
                    //实人认证图标
                    val userIcon =
                        AppHelper.getUserIcon(item.headRealPeople, item.realName, "") ?: return@setOnItemChildClickListener
                    if (userIcon == com.julun.huanque.common.R.mipmap.icon_real_name_home_page
                        && SPUtils.getString(SPParamKey.RealName, "") != BusiConstant.True
                    ) {
                        //显示实名认证
                        realNameNotice()
                        return@setOnItemChildClickListener
                    }
                    if (userIcon == com.julun.huanque.common.R.mipmap.icon_real_people_home_page
                        && SPUtils.getString(SPParamKey.RealPeople, "") != BusiConstant.True
                    ) {
                        realHeaderNotice()
                        return@setOnItemChildClickListener
                    }
//                    CommonDialogFragment.create(
//                        title = "真人照片认证",
//                        content = "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
//                        imageRes = com.julun.huanque.core.R.mipmap.bg_dialog_real_auth,
//                        okText = "去认证",
//                        cancelText = "取消",
//                        callback = CommonDialogFragment.Callback(
//                            onOk = {
//                                (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
//                                    .navigation() as? IRealNameService)?.checkRealHead { e ->
//                                    if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
//                                        MyAlertDialog(requireActivity(), false).showAlertWithOKAndCancel(
//                                            e.busiMessage.toString(),
//                                            title = "修改提示",
//                                            okText = "修改头像",
//                                            noText = "取消",
//                                            callback = MyAlertDialog.MyDialogCallback(onRight = {
//                                                val intent = Intent(requireActivity(), EditInfoActivity::class.java)
//                                                if (ForceUtils.activityMatch(intent)) {
//                                                    startActivity(intent)
//                                                }
//                                            })
//                                        )
//                                    }
//                                }
//
//                            }
//                        )
//                    ).show(requireActivity(), "CommonDialogFragment")
                }
                R.id.rl_guide_photo -> {
                    val intent = Intent(requireActivity(), EditInfoActivity::class.java)
                    if (ForceUtils.activityMatch(intent)) {
                        startActivity(intent)
                    }
                }
            }
        }


//        mLocationService = LocationService(requireContext().applicationContext)
//        mLocationService.registerListener(mLocationListener)
//        mLocationService.setLocationOption(mLocationService.defaultLocationClientOption.apply {
//        })
        sd_header.loadImage(SessionUtils.getHeaderPic(), 100f, 100f)
        tv_go.onClickNew {
            //
            mMainConnectViewModel.heartBeatSwitch.value = HomeTabType.Favorite
        }
        tv_extend.onClickNew {
            val mFilterTagFragment = FilterTagFragment()
            mFilterTagFragment.show(childFragmentManager, "FilterTagFragment")
        }


    }

    //收到其他地方的心都无感操作 自动滑出  后台已经过滤同一天对同一用户的多次喜欢操作或者无感操作做去重 所以这里触发接口没问题
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveLike(event: LikeEvent) {
        lifecycleScope.launchWhenResumed {
            mViewModel.likeEvent.value = event
        }
    }

    //收到主页切换图片index的事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveIndexChange(event: PicChangeEvent) {
        lifecycleScope.launchWhenStarted {
            switchToTargetIndex(event.imageUrl)
        }
    }

    //收到用户上传图片后刷新可查看图片数目
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveCanSeeMaxCountChange(event: CanSeeMaxCountChangeEvent) {
        lifecycleScope.launchWhenStarted {
            mViewModel.currentSeeMaxCoverNum = event.seeMaxCoverNum
            cardsAdapter.notifyDataSetChanged()
        }
    }


    /**
     * 让最顶部的item切换指定的缩略图索引
     */
    private fun switchToTargetIndex(imageUrl: String) {
        val holder = mRecyclerView?.findViewHolderForAdapterPosition(0) as? BaseViewHolder ?: return
        val item = cardsAdapter.getItemOrNull(0) ?: return
        val rvPics = holder.getViewOrNull<RecyclerView>(R.id.rv_pics)
        val tvPicCount = holder.getViewOrNull<TextView>(R.id.tv_pic_count)
        val sdv = holder.getViewOrNull<SimpleDraweeView>(R.id.card_img)
        val sdvBg = holder.getViewOrNull<SimpleDraweeView>(R.id.card_img_bg)
        val guidePhoto = holder.getViewOrNull<RelativeLayout>(R.id.rl_guide_photo)
        val picsAdapter = rvPics?.adapter as? NearbyPicListAdapter ?: return
        val selectIndex: Int = picsAdapter.data.indexOfFirst { it.coverPic == imageUrl }
        item.selectIndex = selectIndex
        if (sdv != null && sdvBg != null && tvPicCount != null) {
            val dataList = picsAdapter.data
            dataList.forEachIndexed { index, homePagePicBean ->
                homePagePicBean.selected = index == selectIndex
            }
            picsAdapter.notifyDataSetChanged()
            val pic = picsAdapter.getItemOrNull(selectIndex) ?: return

            if (pic.blur) {
                guidePhoto?.show()
                sdvBg.show()
                sdv.hide()
                ImageUtils.loadImageWithBlur(sdvBg, pic.coverPic, 2, 150)
            } else {
                guidePhoto?.hide()
//                sdv.loadImageNoResize(pic.coverPic)
                showCardImage(sdv, sdvBg, pic.coverPic)
            }
//            sdv.loadImageNoResize(pic.coverPic)
            tvPicCount.text = "${selectIndex + 1}/${picsAdapter.data.size}"

            val manager = rvPics.layoutManager as AutoCenterLayoutManager
            manager.smoothScrollToPosition(rvPics, selectIndex)
        }


    }

    private fun swipeAutoLike() {
        mReItemTouchHelper.swipeManually(ReItemTouchHelper.RIGHT)
    }

    private fun swipeAutoNoFeel() {
        mReItemTouchHelper.swipeManually(ReItemTouchHelper.LEFT)
    }

    private var noMoreDataError: ResponseError? = null

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.isSuccess()) {
                val bean = it.requireT()
                if (bean.list.isEmpty()) {
                    when {
                        //代表没有更多查看次数了
                        bean.remainTimes == 0 -> {
                            noMoreDataError = ResponseError(0, "今日查看附近次数已经用完，去看看其他喜欢的人吧。")
                        }
                        bean.remainTimes > 0 -> {
                            noMoreDataError = ResponseError(bean.remainTimes, "没有找到附近的人，去看看其他喜欢的人吧。")
                        }
                    }
                    if (bean.isPull) {
                        showLoading(noMoreDataError)
                    }
                    return@Observer
                } else {
//                    bean.list.forEach { nearby ->
//                        nearby.likeTagList.sortList(Comparator { i1, i2 ->
//                            if (nearby.sex == SessionUtils.getSex()) {
//                                val e1 = if (myLikeTagList.contains(i1)) {
//                                    1
//                                } else {
//                                    0
//                                }
//                                val e2 = if (myLikeTagList.contains(i2)) {
//                                    1
//                                } else {
//                                    0
//                                }
//                                e2 - e1
//                            } else {
//                                val e1 = if (myTagList.contains(i1)) {
//                                    1
//                                } else {
//                                    0
//                                }
//                                val e2 = if (myTagList.contains(i2)) {
//                                    1
//                                } else {
//                                    0
//                                }
//                                e2 - e1
//                            }
//
//
//                        })
//                    }
                }
                mRecyclerView.postDelayed({
                    hideLoading()
                    state_layout_error.hide()
                },350)

                mRecyclerView.show()
                if (bean.isPull) {
                    list.clear()
                    list.addAll(bean.list)
                    //首次加载自动播放动画
                    startPlayAni(reset = true)
                } else {
                    list.addAll(bean.list)
                }
                if (bean.myTagList.isNotEmpty()) {
                    myTagList.clear()
                    myTagList.addAll(bean.myTagList)
                }
                if (bean.myLikeTagList.isNotEmpty()) {
                    myLikeTagList.clear()
                    myLikeTagList.addAll(bean.myLikeTagList)
                }

                cardsAdapter.notifyDataSetChanged()

            } else {
                val error = it.error
                if (error != null) {
//                    showLoading(it.error)
                    showLayoutError(2)
                }

            }
        })
        mMainConnectViewModel.refreshNearby.observe(this, Observer {
            if (it != null) {
                val loc = currentLocation ?: return@Observer
                mViewModel.requestNearbyList(
                    QueryType.INIT,
                    loc.latitude,
                    loc.longitude,
                    loc.province,
                    loc.city,
                    loc.district
                )
            }
        })
        mMainConnectViewModel.locationTag.observe(this, Observer {
            if (it != null) {
                currentLocation = it
                val loc = currentLocation ?: return@Observer
                mViewModel.requestNearbyList(
                    QueryType.INIT,
                    loc.latitude,
                    loc.longitude,
                    loc.province,
                    loc.city,
                    loc.district
                )
            } else {
                showLayoutError(1)
            }
        })
        mMainConnectViewModel.nearbyCardGuide.observe(this, Observer {
            if (it != null) {
                //不能过小 不然会闪一下
                if (cardsAdapter.data.size > 4) {
                    cardsAdapter.addData(4, it)
                } else {
                    cardsAdapter.addData(it)
                }

            }
        })
        mViewModel.likeTag.observe(this, Observer {
            if (it != null) {

                if (mViewModel.currentRemainTimes <= 0) {
                    //如果总次数没有了 就不让滑了 任何方向都不行
                    mSwipeDirection = 0
                } else {
                    if (mViewModel.currentHeartTouchTimes <= 0) {
                        mSwipeDirection = ReItemTouchHelper.LEFT
                    } else {
                        mSwipeDirection = ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT
                    }
                }
            }
        })
        mViewModel.likeEvent.observe(this, Observer {
            if (it != null) {
                val item = cardsAdapter.getItemOrNull(0) ?: return@Observer

                if (item.userId == it.userId) {
                    if (it.like) {
                        swipeAutoLike()
                    } else {
                        swipeAutoNoFeel()
                    }
                }
            }
        })
    }

    private fun checkToGetMore() {
        if (noMoreDataError != null) {
            if (list.size <= 0) {
                //已经没有更多数据了 直接显示loading页
                showLoading(noMoreDataError!!)
                noMoreDataError = null
            }
        } else {
            if (list.size < 10) {
                val loc = currentLocation ?: return
                logger.info("当前的列表数目已经小于10 开始取数据")
                mViewModel.requestNearbyList(
                    QueryType.LOAD_MORE,
                    loc.latitude,
                    loc.longitude,
                    loc.province,
                    loc.city,
                    loc.district
                )

            }

        }

    }

    private var loadingAniPlay = true
    private var loadingSetAni: AnimatorSet? = null
    private fun initLoadingAni() {
        waterRv.setCircleColor(ContextCompat.getColor(requireContext(), R.color.colorAccent_lib))
        waterRv.intervalDistance = 50
        waterRv.intervalTime = 20
        waterRv.minRadius = 50
        waterRv.distance = 3
        waterRv.setCircleStrokeWidth(dp2pxf(1))
        waterRv.startMoving()
        val ani1 = ObjectAnimator.ofFloat(sd_header, View.SCALE_X, 1f, 1.3f, 1f)
        val ani2 = ObjectAnimator.ofFloat(sd_header, View.SCALE_Y, 1f, 1.3f, 1f)
        loadingSetAni = AnimatorSet()
        loadingSetAni!!.playTogether(ani1, ani2)
        loadingSetAni!!.duration = 1000L

        loadingSetAni!!.interpolator = AccelerateDecelerateInterpolator()
        loadingSetAni!!.addListener(onEnd = {
            if (loadingAniPlay) {
                loadingSetAni?.startDelay = 200L
                loadingSetAni?.start()
            }

        })
        loadingSetAni!!.start()
    }

    /**
     * 0代表没有更多次数 >0代表没匹配对象
     */
    private fun showLoading(state: ResponseError? = null) {
        state_layout_error.hide()
        state_layout.show()
        mRecyclerView.hide()
        if (loadingSetAni?.isRunning == false) {
            loadingAniPlay = true
            loadingSetAni?.start()
        }
        waterRv.startMoving()

        if (state != null) {
            tv_desc_title.text = state.busiMessage
            tv_go.show()
            when {
                //代表没有更多查看次数了
                state.busiCode == 0 -> {
                    tv_extend.hide()
                }
                state.busiCode > 0 -> {
                    tv_extend.show()
                }
            }

        } else {
            tv_desc_title.text = "正在查找附近的人…"
            tv_go.hide()
        }


    }

    private fun hideLoading() {
        state_layout.hide()
        loadingAniPlay = false
        loadingSetAni?.cancel()
        waterRv.stopMoving()
    }

    /**
     * 0代表没有位置权限 1代表位置定位失败 2代表网络错误
     */
    private fun showLayoutError(type: Int) {
        hideLoading()
        state_layout_error.show()
        mRecyclerView.hide()
        when (type) {
            0 -> {
                iv_error_pic.imageResource = R.mipmap.icon_location_fail
                tv_error_title.text = "开启附近功能"
                tv_error_content.text = "需要启用位置权限，才能看到附近心动的人，请到手机系统设置中开启"
                tv_btn_ok.text = "一键开启"
                tv_btn_ok.onClickNew {
                    //开启定位
                    PhoneUtils.getPermissionSetting(requireActivity().packageName).let {
                        if (ForceUtils.activityMatch(it)) {
                            try {
                                checkPermissionTag = true
                                startActivity(it)
                            } catch (e: Exception) {
                                reportCrash("跳转权限或者默认设置页失败", e)
                            }
                        }
                    }

                }
            }
            1 -> {
                iv_error_pic.imageResource = R.mipmap.icon_location_fail
                tv_error_title.text = "定位失败"
                tv_error_content.text = "无法获取定位，请到手机系统设置中开启定位服务"
                tv_btn_ok.text = "一键开启"
                tv_btn_ok.onClickNew {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, ActivityCodes.REQUEST_CODE_LOCATION)
                }

            }

            2 -> {
                iv_error_pic.imageResource = R.mipmap.icon_net_error
                tv_error_content.text = "请检查网络设置或尝试重新加载"
                tv_btn_ok.text = "重新加载"
                tv_btn_ok.onClickNew {
                    if (!NetUtils.isNetConnected()) {
                        ToastUtils.show("网络异常")
                        return@onClickNew
                    }
                    checkPermission()
                }

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logger.info("requestCode=${requestCode}")
        if (requestCode == ActivityCodes.REQUEST_CODE_LOCATION) {
            mMainConnectViewModel.startLocation()
        }
    }
    //封装百度地图相关的Service
//    private lateinit var mLocationService: LocationService


    private var currentLocation: BDLocation? = null

    //百度地图监听的Listener
//    private var mLocationListener = object : BDAbstractLocationListener() {
//        override fun onReceiveLocation(location: BDLocation?) {
//            logger.info("location error=${location?.locTypeDescription}")
//            currentLocation = location
//            if (null != location && location.locType != BDLocation.TypeServerError && location.locType != BDLocation.TypeCriteriaException) {
//                logger.info("location=${location.addrStr}")
//                //获得一次结果，就结束定位
//                stopLocation()
//
//                val loc = currentLocation ?: return
//                mViewModel.requestNearbyList(QueryType.INIT, loc.latitude, loc.longitude, loc.province, loc.city, loc.district)
//            } else {
//                ToastUtils.show("无法获取定位 请确保已打开定位开关")
//                checkPermissionTag = true
//            }
//        }
//    }


    private var checkPermissionTag = false

    /**
     * 检查定位权限
     */
    private fun checkPermission() {
        showLoading()
        val rxPermissions = RxPermissions(requireActivity())
        rxPermissions
            .requestEachCombined(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
//                        mLocationService.start()
                        mMainConnectViewModel.startLocation()
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // Oups permission denied
                        logger.info("获取定位被拒绝")
//                        showOpenLocationDialog()
                        showLayoutError(0)
                    }
                    else -> {
                        logger.info("获取定位被永久拒绝")
//                        showOpenLocationDialog()
                        showLayoutError(0)
                    }
                }

            }
    }

    private fun showOpenLocationDialog() {
        CommonDialogFragment.create(
            commonDialogFragment, "开启附近功能",
            "需要启用位置权限，才能看到附近心动的人\n" +
                    "请到手机系统设置中开启",
            imageRes = R.mipmap.bg_header_living,
            okText = "一键开启",
            cancelable = false,
            callback = CommonDialogFragment.Callback(
                onOk = {
                    PhoneUtils.getPermissionSetting(requireActivity().packageName).let {
                        if (ForceUtils.activityMatch(it)) {
                            try {
                                checkPermissionTag = true
                                startActivity(it)
                            } catch (e: Exception) {
                                reportCrash("跳转权限或者默认设置页失败", e)
                            }
                        }
                    }

                })
        ).show(requireActivity(), "CommonDialogFragment")
    }

    /**
     * 停止定位
     */
//    private fun stopLocation() {
////        mLocationService.stop()
//    }

    override fun onResume() {
        super.onResume()
        if (checkPermissionTag) {
            checkPermissionTag = false
            checkPermission()
        }
        startPlayAni(reset = true)
    }

    override fun onStop() {
        super.onStop()
        stopAni()
    }

    override fun onDestroy() {
//        mLocationService.unregisterListener(mLocationListener)
        loadingAniPlay = false
        loadingSetAni?.cancel()
        super.onDestroy()
    }

    private var currentAniIndex: Int = 0

    private fun randomLocation(rd: View) {
        //随机位置
        val rdLp = rd.layoutParams as ConstraintLayout.LayoutParams
        val dist = max((randomDistance * Random.nextFloat()).toInt(), dp2px(10))
//        logger.info("随机的距离=$dist")
        if (rd.id == R.id.ani_tag_01 || rd.id == R.id.ani_tag_02) {
            rdLp.topMargin = dist+dp2px(80)
        } else {
            rdLp.bottomMargin = dist
        }


    }

    private fun startPlayAni(reset: Boolean = false) {

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED))
            mRecyclerView.postDelayed({
                if (reset) {
                    currentAniIndex = 0
                    stopAni()
                }
                //获取到当前最上方的item
                val item = cardsAdapter.getItemOrNull(0) ?: return@postDelayed
                val tags = item.tagList
                if (tags.isEmpty()) {
                    return@postDelayed
                }
                val first: UserTagBean? = tags.getOrNull(currentAniIndex)
                currentAniIndex++
                val second: UserTagBean? = tags.getOrNull(currentAniIndex)
                currentAniIndex++

                if (first == null && second == null) {
                    logger.info("没有数据了 停止动画")
                    startPlayAni(true)
                    return@postDelayed
                }

                val holder = mRecyclerView?.findViewHolderForAdapterPosition(0) as? BaseViewHolder ?: return@postDelayed
                val aniViewArray1 = mutableListOf<HomeCardTagView>(
                    holder.getView<HomeCardTagView>(R.id.ani_tag_01),
//                holder.getView<HomeCardTagView>(R.id.ani_tag_02),
                    holder.getView<HomeCardTagView>(R.id.ani_tag_03)
//                holder.getView<HomeCardTagView>(R.id.ani_tag_04)
                )
                val aniViewArray2 = mutableListOf<HomeCardTagView>(
//                holder.getView<HomeCardTagView>(R.id.ani_tag_01),
                    holder.getView<HomeCardTagView>(R.id.ani_tag_02),
//                holder.getView<HomeCardTagView>(R.id.ani_tag_03),
                    holder.getView<HomeCardTagView>(R.id.ani_tag_04)
                )
                val rd1 = aniViewArray1.random()
                val rd2 = aniViewArray2.random()
//            logger.info("开始做标签显隐动画 rd1=${rd1.id} rd2=${rd2.id}")

                rd1.removeListener()
                rd2.removeListener()
                randomLocation(rd1)
                randomLocation(rd2)

                rd1.listener = object : Animator.AnimatorListener {
                    var isCancelTag = false
                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
//                    logger.info("一次动画完成")
                        if (isCancelTag) {
                            isCancelTag = false
                        } else {
                            startPlayAni()
                        }

                    }

                    override fun onAnimationCancel(animation: Animator?) {
//                    logger.info("一次动画取消")
                        isCancelTag = true
                    }

                    override fun onAnimationStart(animation: Animator?) {

                    }
                }

                val delay = if (!reset) 0 else 1000L
                if (first != null) {
                    rd1.startSetDataAndAni(first, delay)
                }
                if (second != null) {
                    rd2.startSetDataAndAni(second, delay)
                }

            }, 50)


    }


    private var lastDoTime: Long = 0
    private fun stopAni() {
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime - lastDoTime >= 200) {
            lastDoTime = currentTime
            logger.info("停止动画")
            val item = cardsAdapter.getItemOrNull(0) ?: return
            val holder = mRecyclerView?.findViewHolderForAdapterPosition(0) as? BaseViewHolder ?: return
            holder.getView<HomeCardTagView>(R.id.ani_tag_01).stopAni()
            holder.getView<HomeCardTagView>(R.id.ani_tag_02).stopAni()
            holder.getView<HomeCardTagView>(R.id.ani_tag_03).stopAni()
            holder.getView<HomeCardTagView>(R.id.ani_tag_04).stopAni()
        }

    }

    private fun showCardImage(sdv: SimpleDraweeView, sdvBg: SimpleDraweeView, coverPic: String) {
        sdvBg.hide()
        sdv.show()
//                sdv.loadImageNoResize(item.coverPic)
        ImageUtils.loadImageWithListener(sdv, coverPic, object : BaseControllerListener<ImageInfo>() {
            override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, anim: Animatable?) {
                if (imageInfo == null) {
                    return
                }
                val height = imageInfo.height
                val width = imageInfo.width
                logger.info("宽高比：${width / height.toFloat()}")
                val layoutParams = sdv.layoutParams as ConstraintLayout.LayoutParams
                if (width / height.toFloat() >= 7.0f / 9.0f) {
                    sdv.hierarchy.roundingParams = RoundingParams.fromCornersRadius(0f)
                    layoutParams.dimensionRatio = "h,${width}:${height}"
                    sdvBg.show()
                    ImageUtils.loadImageWithBlur(sdvBg, coverPic, 2, 150)
                } else {
                    sdv.hierarchy.roundingParams = RoundingParams.fromCornersRadius(dp2pxf(10))
                    layoutParams.dimensionRatio = "0"
                    sdvBg.hide()
                }
                sdv.requestLayout()

            }

            override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                Log.d("TAG", "Intermediate image received")
            }

            override fun onFailure(id: String?, throwable: Throwable?) {
                throwable!!.printStackTrace()
            }
        }
        )
    }

    private val cardsAdapter: BaseQuickAdapter<NearbyUserBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<NearbyUserBean, BaseViewHolder>(R.layout.item_user_swip_card, list) {
            init {
                addChildClickViewIds(
                    R.id.cl_container,
                    R.id.ani_tag_01,
                    R.id.ani_tag_02,
                    R.id.ani_tag_03,
                    R.id.ani_tag_04,
                    R.id.iv_super_like,
                    R.id.tv_bottom_tips,
                    R.id.iv_auth_tag,
                    R.id.rl_guide_photo
                )
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val tempData = getItemOrNull(position)
                if (tempData != null) {
                    val card_img = holder.getViewOrNull<SimpleDraweeView>(R.id.card_img)
                    if (card_img != null) {
                        ViewCompat.setTransitionName(card_img, "Image${tempData.userId}")
                        //                    val tv_user_name = holder.getView<TextView>(R.id.tv_user_name)
//                    ViewCompat.setTransitionName(tv_user_name, "TextView${tempData.userId}")
                    }
                }

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val holder = super.onCreateViewHolder(parent, viewType)
                //注意viewType 普通类型默认是0 如果有其他空白页或者头部底部就会不同type 这里就会找不到
                if (viewType == 0) {
                    holder.getView<HomeCardTagView>(R.id.ani_tag_01).setLeftDot(true)
                    holder.getView<HomeCardTagView>(R.id.ani_tag_02).setLeftDot(false)
                    holder.getView<HomeCardTagView>(R.id.ani_tag_03).setLeftDot(true)
                    holder.getView<HomeCardTagView>(R.id.ani_tag_04).setLeftDot(false)
                }

                return holder
            }

            override fun convert(holder: BaseViewHolder, item: NearbyUserBean) {
                val iv_auth_tag = holder.getView<ImageView>(R.id.iv_auth_tag)

                val userIcon = AppHelper.getUserIcon(item.headRealPeople, item.realName, "")
                if (userIcon == null) {
                    iv_auth_tag.hide()
                } else {
                    iv_auth_tag.show()
                    iv_auth_tag.setImageResource(userIcon)
                }
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                val sdvBg = holder.getView<SimpleDraweeView>(R.id.card_img_bg)
                val guidePhoto = holder.getView<RelativeLayout>(R.id.rl_guide_photo)
                guidePhoto.hide()
//                showCardImage(sdv, sdvBg, item.coverPic)
                if (item.cardType == CardType.GUIDE) {
                    holder.setGone(R.id.iv_dislike, true)
                    holder.setGone(R.id.iv_like, true)
                    holder.setGone(R.id.tv_user_name, true)
                    holder.setGone(R.id.tv_bottom_tips, true)
                    holder.setGone(R.id.rv_pics, true)
                    holder.setGone(R.id.tv_pic_count, true)
                    holder.setGone(R.id.ll_info, true)
                    holder.setGone(R.id.tv_top_right_tips, true)
                    showCardImage(sdv, sdvBg, item.coverPic)
                    return
                } else {
                    holder.setGone(R.id.iv_dislike, false)
                    holder.setGone(R.id.iv_like, false)
                    holder.setGone(R.id.tv_user_name, false)
                    holder.setGone(R.id.tv_bottom_tips, false)
                    holder.setGone(R.id.rv_pics, false)
                    holder.setGone(R.id.tv_pic_count, false)
                    holder.setGone(R.id.ll_info, false)
                    holder.setGone(R.id.tv_top_right_tips, false)
                }
//                if (item.distance != 0) {
//                    holder.setText(R.id.tv_distance, "${item.distance}")
//                        .setVisible(R.id.tv_distance, true)
//                    holder.setText(R.id.tv_locationAge, "km ${item.area} / ${item.age}岁")
//                } else {
//                    holder.setText(R.id.tv_locationAge, "${item.area} / ${item.age}岁")
//                        .setGone(R.id.tv_distance, true)
//                }
                val tvDistance = holder.getView<TextView>(R.id.tv_distance)
                val ivDistance = holder.getView<ImageView>(R.id.iv_distance)
                val age = if (item.age > 0) {
                    " / ${item.age}岁"
                } else {
                    ""
                }
                if (item.distance > 0) {
                    if (item.sameCity) {
                        tvDistance.show()
                        ivDistance.hide()
                        when {
                            item.distance < 1000 -> {
                                tvDistance.text = "${item.distance}"
                                holder.setText(R.id.tv_locationAge, "m ${item.area}${age}")
                            }
                            else -> {
                                val format = DecimalFormat("#.0")
                                format.roundingMode = RoundingMode.DOWN
                                val dt = format.format((item.distance / 1000.0))
                                tvDistance.text = dt
                                holder.setText(R.id.tv_locationAge, "km ${item.area}${age}")
                            }
                        }
                    } else {
                        tvDistance.hide()
                        ivDistance.show()
                        holder.setText(R.id.tv_locationAge, "${item.area}${age}")
                        when {
                            item.distance <= 100000 -> {
                                ivDistance.imageResource = R.mipmap.icon_home_distance_car
                            }
                            item.distance <= 800000 -> {

                                //显示动车
                                ivDistance.imageResource = R.mipmap.icon_home_distance_rail_way
                            }
                            else -> {
                                ivDistance.show()
                                //显示飞机
                                ivDistance.imageResource = R.mipmap.icon_home_distance_air_plan
                            }
                        }

                    }

                } else {
                    tvDistance.hide()
                    ivDistance.show()
                    val starList = mutableListOf<String>("金星", "木星", "水星", "火星", "土星")
                    val currentStar = starList.random()
                    ivDistance.imageResource = R.mipmap.icon_home_distance_rocket
                    holder.setText(R.id.tv_locationAge, "$currentStar${age}")
                }


                holder.setText(R.id.tv_user_name, item.nickname)
                if (item.interactTips.isNotEmpty()) {
                    holder.setText(R.id.tv_top_right_tips, item.interactTips).setGone(R.id.tv_top_right_tips, false)
                } else {
                    holder.setGone(R.id.tv_top_right_tips, true)
                }
                if (item.likeTagList.isEmpty()) {
//                    holder.setText(R.id.tv_bottom_tips, "TA还没有喜欢的标签，邀请TA填写吧")
                    holder.setGone(R.id.tv_bottom_tips, true)
                } else {
//                    val sameList = mutableListOf<UserTagBean>()
                    var tagsStr: String = ""
                    tagsStr = when (item.likeTagList.size) {
                        1 -> {
                            item.likeTagList[0].tagName
                        }
                        2 -> {
                            item.likeTagList[0].tagName + "、" + item.likeTagList[1].tagName
                        }
                        3 -> {
                            item.likeTagList[0].tagName + "、" + item.likeTagList[1].tagName + "、" + item.likeTagList[2].tagName
                        }
                        else -> {
                            item.likeTagList[0].tagName + "、" + item.likeTagList[1].tagName + "、" + item.likeTagList[2].tagName + "..."
                        }
                    }
                    val content = "TA喜欢：${tagsStr}"
                    val styleSpan1A = StyleSpan(Typeface.BOLD)
//                    val styleSpan1A = RelativeSizeSpan(1.1f)
//                    val styleSpan1B = ForegroundColorSpan(Color.parseColor("#FFCC00"))
                    val start = content.indexOf(tagsStr)
                    val end = start + tagsStr.length
                    val sp = SpannableString(content)
                    sp.setSpan(styleSpan1A, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
//                    sp.setSpan(styleSpan1B, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    holder.setText(R.id.tv_bottom_tips, sp).setVisible(R.id.tv_bottom_tips, true)
                }


                val rvPics = holder.getView<RecyclerView>(R.id.rv_pics)
                val rvLp = rvPics.layoutParams
                if (item.coverPicList.size <= 4) {
                    rvLp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    rvLp.width = dp2px(140)
                }
                rvPics.layoutManager = AutoCenterLayoutManager(context, RecyclerView.HORIZONTAL, false)
                val mPicsAdapter: NearbyPicListAdapter
                if (rvPics.adapter != null) {
                    mPicsAdapter = rvPics.adapter as NearbyPicListAdapter
                } else {
                    mPicsAdapter = NearbyPicListAdapter()
                    rvPics.adapter = mPicsAdapter
                }
                if (rvPics.itemDecorationCount <= 0) {
                    rvPics.addItemDecoration(
                        HorizontalItemDecoration(dp2px(6))
                    )
                }
                val list = mutableListOf<HomePagePicBean>()
                item.coverPicList.forEachIndexed { index, pic ->

                    if (index == 0) {
                        list.add(HomePagePicBean(pic, selected = index == item.selectIndex))
                    } else {
                        val mBlur = if (mViewModel.currentSeeMaxCoverNum == -1) {
                            false
                        } else {
                            index >= mViewModel.currentSeeMaxCoverNum
                        }
                        list.add(HomePagePicBean(pic, selected = index == item.selectIndex, blur = mBlur))
                    }

                }
                mPicsAdapter.setList(list)

                val tvPicCount = holder.getView<TextView>(R.id.tv_pic_count)

                selectPic(item.selectIndex, mPicsAdapter, sdv, sdvBg, tvPicCount, guidePhoto)
                val manager = rvPics.layoutManager as AutoCenterLayoutManager
                manager.smoothScrollToPosition(rvPics, item.selectIndex)

                mPicsAdapter.onAdapterClickNew { _, _, position ->
                    val itemPic = mPicsAdapter.getItemOrNull(position)
                    if (itemPic?.selected == true) {
                        return@onAdapterClickNew
                    }

                    item.selectIndex = position
                    selectPic(position, mPicsAdapter, sdv, sdvBg, tvPicCount, guidePhoto)
//                    val manager = rvPics.layoutManager as AutoCenterLayoutManager
                    manager.smoothScrollToPosition(rvPics, position)
                }

//                tvPicCount.text = "1/${mPicsAdapter.data.size}"
            }

            /**
             * 选中特定的图片
             */
            private fun selectPic(
                position: Int,
                mPicsAdapter: NearbyPicListAdapter,
                sdv: SimpleDraweeView,
                sdvBg: SimpleDraweeView,
                tv: TextView,
                guideView: View
            ) {
                val dataList = mPicsAdapter.data
                dataList.forEachIndexed { index, homePagePicBean ->
                    homePagePicBean.selected = index == position
                }
                mPicsAdapter.notifyDataSetChanged()
                val pic = mPicsAdapter.getItemOrNull(position) ?: return
                if (pic.blur) {
                    sdv.hide()
                    sdvBg.show()
                    guideView.show()
                    ImageUtils.loadImageWithBlur(sdvBg, pic.coverPic, 2, 150)
                } else {
                    guideView.hide()
//                    sdv.loadImageNoResize(pic.coverPic)
                    showCardImage(sdv, sdvBg, pic.coverPic)
                }

                tv.text = "${position + 1}/${mPicsAdapter.data.size}"
            }

        }
    }

    private fun realHeaderNotice() {
        CommonDialogFragment.create(
            title = "真人照片认证",
            content = "通过人脸识别技术确认照片为真人将获得认证标识，提高交友机会哦~",
            imageRes = R.mipmap.bg_dialog_real_auth,
            okText = "去认证",
            cancelText = "以后再说",
            callback = CommonDialogFragment.Callback(
                onOk = {
                    (ARouter.getInstance().build(ARouterConstant.REALNAME_SERVICE)
                        .navigation() as? IRealNameService)?.checkRealHead { e ->
                        if (e is ResponseError && e.busiCode == ErrorCodes.REAL_HEAD_ERROR) {
                            MyAlertDialog(requireActivity(), false).showAlertWithOKAndCancel(
                                e.busiMessage.toString(),
                                title = "修改提示",
                                okText = "修改头像",
                                noText = "取消",
                                callback = MyAlertDialog.MyDialogCallback(onRight = {
                                    val intent = Intent(requireContext(), EditInfoActivity::class.java)
                                    if (ForceUtils.activityMatch(intent)) {
                                        startActivity(intent)
                                    }
                                })
                            )
                        }
                    }

                }
            )
        ).show(requireActivity(), "CommonDialogFragment")

    }

    private fun realNameNotice() {
        CommonDialogFragment.create(
            title = "实名认证",
            content = "完成实名认证，提高真人交友可信度，将获得更多推荐机会~",
            imageRes = R.mipmap.bg_dialog_real_name,
            okText = "去认证",
            cancelText = "以后再说",
            callback = CommonDialogFragment.Callback(
                onOk = {
                    ARouter.getInstance().build(ARouterConstant.REAL_NAME_MAIN_ACTIVITY)
                        .navigation()
                }
            )
        ).show(requireActivity(), "CommonDialogFragment")

    }

}