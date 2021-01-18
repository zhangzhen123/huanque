package com.julun.huanque.core.ui.main.heartbeat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.base.dialog.CommonDialogFragment
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.ResponseError
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.bean.beans.NearbyUserBean
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.bean.events.LikeEvent
import com.julun.huanque.common.bean.events.PicChangeEvent
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.HomeTabType
import com.julun.huanque.common.helper.reportCrash
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
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.homepage.TagFragment
import com.julun.huanque.core.ui.tag_manager.TagUserPicsActivity
import com.julun.huanque.core.viewmodel.MainConnectViewModel
import com.julun.huanque.core.widgets.HomeCardTagView
import com.julun.maplib.LocationService
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

            override fun getSwipeThreshold(): Float {
                return 0.15f
            }

            override fun getCardScale(): Float {
                return 0.05f
            }

            override fun getSwipeOutAnimDuration(): Int {
                return 200
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
                        logger.info("swiping direction=right ratio=${ratio}")
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
                currentAniIndex = 0
                startPlayAni()
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
                    }
                    ReItemTouchHelper.RIGHT -> {
                        logger.info("onSwipedClear direction=RIGHT mViewModel.currentHeartTouchTimes=${mViewModel.currentHeartTouchTimes}")
                        if (mViewModel.currentHeartTouchTimes <= 0) {
                            //
                            logger.info("提示弹窗 心动次数不足")
                            CommonDialogFragment.create(
                                commonDialogFragment, "心动数已达到上限",
                                mViewModel.heartTouchNotEnoughTips,
                                imageRes = R.mipmap.bg_header_living,
                                okText = "我知道了"
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
                R.id.card_img -> {
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
                    val pic = tempBean.coverPicList.getOrNull(tempBean.selectIndex) ?: ""
                    HomePageActivity.newInstance(requireActivity(), item.userId, showPic = pic)
                }
                R.id.tv_bottom_tips -> {
                    if (item.likeTagList.isEmpty()) {
                        ToastUtils.show("已邀请TA添加喜欢的标签")
                    } else {
                        item.likeTagList.forEach {
                            if (myTagList.contains(it)) {
                                it.mark = BooleanType.TRUE
                            } else {
                                it.mark = BooleanType.FALSE
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

    /**
     * 让最顶部的item切换指定的缩略图索引
     */
    private fun switchToTargetIndex(imageUrl: String) {
        val holder = mRecyclerView?.findViewHolderForAdapterPosition(0) as? BaseViewHolder ?: return
        val item = cardsAdapter.getItemOrNull(0) ?: return
        val rvPics = holder.getViewOrNull<RecyclerView>(R.id.rv_pics)
        val tvPicCount = holder.getViewOrNull<TextView>(R.id.tv_pic_count)
        val sdv = holder.getViewOrNull<SimpleDraweeView>(R.id.card_img)
        val picsAdapter = rvPics?.adapter as? NearbyPicListAdapter ?: return
        val selectIndex: Int = picsAdapter.data.indexOfFirst { it.coverPic == imageUrl }
        item.selectIndex = selectIndex
        if (sdv != null && tvPicCount != null) {
            val dataList = picsAdapter.data
            dataList.forEachIndexed { index, homePagePicBean ->
                if (index == selectIndex) {
                    homePagePicBean.selected = BusiConstant.True
                } else {
                    homePagePicBean.selected = BusiConstant.False
                }
            }
            picsAdapter.notifyDataSetChanged()

            val pic = picsAdapter.getItemOrNull(selectIndex) ?: return
            sdv.loadImageNoResize(pic.coverPic)
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
                    if (it.isRefresh()) {
                        showLoading(noMoreDataError)
                    }
                    return@Observer
                }

                hideLoading()
                state_layout_error.hide()
                mRecyclerView.show()
                if (it.isRefresh()) {
                    list.clear()
                    list.addAll(bean.list)
                } else {
                    list.addAll(bean.list)
                }

                if (bean.myTagList.isNotEmpty()) {
                    myTagList.clear()
                    myTagList.addAll(bean.myTagList)
                }

                cardsAdapter.notifyDataSetChanged()
                if (it.isRefresh()) {
                    //首次加载自动播放动画
                    currentAniIndex = 0
                    startPlayAni()
                }
            } else {
                val error = it.error
                if (error != null) {
//                    showLoading(it.error)
                    showLayoutError(1)
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
                currentLocation=it
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
        val ani1 = ObjectAnimator.ofFloat(sd_header, View.SCALE_X, 1.3f, 1f, 1.3f)
        val ani2 = ObjectAnimator.ofFloat(sd_header, View.SCALE_Y, 1.3f, 1f, 1.3f)
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
     * 0代表位置异常 1代表网络错误
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
            callback = CommonDialogFragment.MyDialogCallback(
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
        logger.info("随机的距离=$dist")
        if (rd.id == R.id.ani_tag_01 || rd.id == R.id.ani_tag_02) {
            rdLp.topMargin = dist
        } else {
            rdLp.bottomMargin = dist
        }


    }

    private fun startPlayAni() {
        mRecyclerView.postDelayed({
            //获取到当前最上方的item
            val item = cardsAdapter.getItemOrNull(0) ?: return@postDelayed
            val holder = mRecyclerView?.findViewHolderForAdapterPosition(0) as? BaseViewHolder ?: return@postDelayed
            val aniViewArray = mutableListOf<HomeCardTagView>(
                holder.getView<HomeCardTagView>(R.id.ani_tag_01),
                holder.getView<HomeCardTagView>(R.id.ani_tag_02),
                holder.getView<HomeCardTagView>(R.id.ani_tag_03),
                holder.getView<HomeCardTagView>(R.id.ani_tag_04)
            )
            val rd1 = aniViewArray.random()
            val rd2 = aniViewArray.filter { it.id != rd1.id }.random()
            logger.info("开始做标签显隐动画 rd1=${rd1.id} rd2=${rd2.id}")

            val tags = item.tagList
            val first: UserTagBean? = tags.getOrNull(currentAniIndex)
            currentAniIndex++
            val second: UserTagBean? = tags.getOrNull(currentAniIndex)
            currentAniIndex++

            if (first == null && second == null) {
                logger.info("没有数据了 停止动画")
                return@postDelayed
            }
//            rd1.removeListener()
//            rd2.removeListener()
            randomLocation(rd1)
            randomLocation(rd2)

            rd1.listener = object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    logger.info("一次动画完成")
                    startPlayAni()
                }

                override fun onAnimationCancel(animation: Animator?) {
                    logger.info("一次动画取消")
                }

                override fun onAnimationStart(animation: Animator?) {

                }
            }

            if (first != null) {
                rd1.startSetDataAndAni(first)
            }
            if (second != null) {
                rd2.startSetDataAndAni(second)
            }

        }, 50)


    }


    private var lastDoTime: Long = 0
    private fun stopAni() {
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime - lastDoTime >= 300) {
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

    private val cardsAdapter: BaseQuickAdapter<NearbyUserBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<NearbyUserBean, BaseViewHolder>(R.layout.item_user_swip_card, list) {
            init {
                addChildClickViewIds(
                    R.id.card_img,
                    R.id.ani_tag_01,
                    R.id.ani_tag_02,
                    R.id.ani_tag_03,
                    R.id.ani_tag_04,
                    R.id.iv_super_like,
                    R.id.tv_bottom_tips
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
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.coverPic)
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
                    tvDistance.show()
                    if (item.sameCity) {
                        ivDistance.hide()
                    } else {
                        ivDistance.show()
//                        holder.setText(R.id.tv_locationAge, "${item.area}${age}")
                        when {
                            item.distance < 100000 -> {
                                ivDistance.imageResource = R.mipmap.icon_home_distance_car
                            }
                            item.distance < 800000 -> {

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
                    holder.setText(R.id.tv_bottom_tips, "TA还没有喜欢的标签，邀请TA填写吧")
                } else {
//                    val sameList = mutableListOf<UserTagBean>()
                    item.likeTagList.sortList(Comparator { i1, i2 ->
                        if (item.sex == SessionUtils.getSex()) {

                        } else {

                        }
                        val e1 = if (myTagList.contains(i1)) {
                            1
                        } else {
                            0
                        }
                        val e2 = if (myTagList.contains(i2)) {
                            1
                        } else {
                            0
                        }
                        e2 - e1
                    })
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
                    val content = "TA喜欢:${tagsStr}"
                    val styleSpan1A = RelativeSizeSpan(1.1f)
                    val styleSpan1B = ForegroundColorSpan(Color.parseColor("#FFCC00"))
                    val start = content.indexOf(tagsStr)
                    val end = start + tagsStr.length
                    val sp = SpannableString(content)
                    sp.setSpan(styleSpan1A, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    sp.setSpan(styleSpan1B, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
                    holder.setText(R.id.tv_bottom_tips, sp)
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
                        list.add(HomePagePicBean(pic, selected = BooleanType.TRUE))
                    } else {
                        list.add(HomePagePicBean(pic, selected = BooleanType.FALSE))
                    }

                }
                mPicsAdapter.setList(list)
                val tvPicCount = holder.getView<TextView>(R.id.tv_pic_count)

                mPicsAdapter.onAdapterClickNew { _, _, position ->
                    val itemPic = mPicsAdapter.getItemOrNull(position)
                    if (itemPic?.selected == BusiConstant.True) {
                        return@onAdapterClickNew
                    }

                    item.selectIndex = position
                    selectPic(position, mPicsAdapter, sdv, tvPicCount)
                    val manager = rvPics.layoutManager as AutoCenterLayoutManager
                    manager.smoothScrollToPosition(rvPics, position)
                }

                tvPicCount.text = "1/${mPicsAdapter.data.size}"
            }

            /**
             * 选中特定的图片
             */
            private fun selectPic(position: Int, mPicsAdapter: NearbyPicListAdapter, sdv: SimpleDraweeView, tv: TextView) {
                val dataList = mPicsAdapter.data
                dataList.forEachIndexed { index, homePagePicBean ->
                    if (index == position) {
                        homePagePicBean.selected = BusiConstant.True
                    } else {
                        homePagePicBean.selected = BusiConstant.False
                    }
                }
                mPicsAdapter.notifyDataSetChanged()
                val pic = mPicsAdapter.getItemOrNull(position) ?: return
                sdv.loadImageNoResize(pic.coverPic)
                tv.text = "${position + 1}/${mPicsAdapter.data.size}"
            }

        }
    }


}