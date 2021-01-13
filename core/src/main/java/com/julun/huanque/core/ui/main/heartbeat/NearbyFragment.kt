package com.julun.huanque.core.ui.main.heartbeat

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
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
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.constant.HomeTabType
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.reportCrash
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ForceUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.SessionUtils
import com.julun.huanque.common.utils.device.PhoneUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.cardlib.CardLayoutManager
import com.julun.huanque.common.widgets.cardlib.CardSetting
import com.julun.huanque.common.widgets.cardlib.CardTouchHelperCallback
import com.julun.huanque.common.widgets.cardlib.OnSwipeCardListener
import com.julun.huanque.common.widgets.cardlib.utils.ReItemTouchHelper
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.NearbyPicListAdapter
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.tag_manager.TagUserPicsActivity
import com.julun.huanque.core.viewmodel.MainConnectViewModel
import com.julun.huanque.core.widgets.HomeCardTagView
import com.julun.maplib.LocationService
import kotlinx.android.synthetic.main.fragment_nearby.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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
                return 10f
            }

            override fun getCardTranslateDistance(): Int {
                return 30
            }

            override fun enableHardWare(): Boolean {
                return false
            }

            override fun getSwipeThreshold(): Float {
                return 0.35f
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
                    val shareView = parentView.findViewById<View>(R.id.card_img)
                    val tv_user_name = parentView.findViewById<View>(R.id.tv_user_name)

                    val intent = Intent(requireActivity(), HomePageActivity::class.java)
                    val pair1: androidx.core.util.Pair<View, String> =
                        androidx.core.util.Pair(shareView, ViewCompat.getTransitionName(shareView) ?: "")
                    val pair2: androidx.core.util.Pair<View, String> =
                        androidx.core.util.Pair(tv_user_name, ViewCompat.getTransitionName(tv_user_name) ?: "")

                    /**
                     * 4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
                     */
                    val activityOptionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), pair1, pair2)

                    intent.putExtra(ParamConstant.UserId, tempBean.userId)
                    intent.putExtra(ParamConstant.NearByBean, tempBean)
                    startActivity(intent, activityOptionsCompat.toBundle())
//                    HomePageActivity.newInstance(requireActivity(), item.userId)
                }
            }
        }



        mLocationService = LocationService(requireContext().applicationContext)
        mLocationService.registerListener(mLocationListener)
        mLocationService.setLocationOption(mLocationService.defaultLocationClientOption.apply {
        })
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
                mRecyclerView.show()
                list.addAll(bean.list)
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
                    showLoading(it.error)
                }

            }
        })
        mMainConnectViewModel.refreshNearby.observe(this, Observer {
            if (it != null) {
                val loc = currentLocation ?: return@Observer
                logger.info("当前的列表数目已经小于10 开始取数据")
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

    //封装百度地图相关的Service
    private lateinit var mLocationService: LocationService


    private var currentLocation: BDLocation? = null

    //百度地图监听的Listener
    private var mLocationListener = object : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            logger.info("location error=${location?.locTypeDescription}")
            currentLocation = location
            if (null != location && location.locType != BDLocation.TypeServerError) {
                logger.info("location=${location.addrStr}")
                //获得一次结果，就结束定位
                stopLocation()

                val loc = currentLocation ?: return
                mViewModel.requestNearbyList(QueryType.INIT, loc.latitude, loc.longitude, loc.province, loc.city, loc.district)
            }
        }
    }


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
                        mLocationService.start()
                    }
                    permission.shouldShowRequestPermissionRationale -> {
                        // Oups permission denied
                        logger.info("获取定位被拒绝")
                        showOpenLocationDialog()
                    }
                    else -> {
                        logger.info("获取定位被永久拒绝")
                        showOpenLocationDialog()
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
    private fun stopLocation() {
        mLocationService.stop()
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissionTag) {
            checkPermissionTag = false
            checkPermission()
        }

    }

    override fun onStop() {
        super.onStop()
        stopLocation()
        stopAni()
    }

    override fun onDestroy() {
        mLocationService.unregisterListener(mLocationListener)
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
                    R.id.iv_super_like
                )
            }

            override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val tempData = getItemOrNull(position)
                if (tempData != null) {
                    val card_img = holder.getView<SimpleDraweeView>(R.id.card_img)
                    ViewCompat.setTransitionName(card_img, "Image${tempData.userId}")
                    val tv_user_name = holder.getView<TextView>(R.id.tv_user_name)
                    ViewCompat.setTransitionName(tv_user_name, "TextView${tempData.userId}")
                }

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
                val holder = super.onCreateViewHolder(parent, viewType)
                holder.getView<HomeCardTagView>(R.id.ani_tag_01).setLeftDot(true)
                holder.getView<HomeCardTagView>(R.id.ani_tag_02).setLeftDot(false)
                holder.getView<HomeCardTagView>(R.id.ani_tag_03).setLeftDot(true)
                holder.getView<HomeCardTagView>(R.id.ani_tag_04).setLeftDot(false)


                return holder
            }

            override fun convert(holder: BaseViewHolder, item: NearbyUserBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.coverPic)
                if (item.distance != 0) {
                    holder.setText(R.id.tv_distance, "${item.distance}")
                        .setVisible(R.id.tv_distance, true)
                    holder.setText(R.id.tv_locationAge, "km ${item.area} / ${item.age}岁")
                } else {
                    holder.setText(R.id.tv_locationAge, "${item.area} / ${item.age}岁")
                        .setGone(R.id.tv_distance, true)
                }


                holder.setText(R.id.tv_user_name, item.nickname)
                if (item.interactTips.isNotEmpty()) {
                    holder.setText(R.id.tv_top_right_tips, item.interactTips).setGone(R.id.tv_top_right_tips, false)
                } else {
                    holder.setGone(R.id.tv_top_right_tips, true)
                }
                if (item.likeTagList.isEmpty()) {
                    holder.setText(R.id.tv_bottom_tips, "TA还没有喜欢的标签，邀请TA填写吧>")
                } else {
                    val sameList = mutableListOf<UserTagBean>()
                    item.likeTagList.forEach {
                        if (myTagList.contains(it)) {
                            sameList.add(it)
                        }
                    }
                    var tagsStr: String = ""
                    when {
                        sameList.isEmpty() -> {
                        }
                        sameList.size == 1 -> {
                            tagsStr = item.likeTagList[0].tagName + "等"
                        }
                        sameList.size > 1 -> {
                            tagsStr = item.likeTagList[0].tagName + "、" + item.likeTagList[1].tagName + "等"
                        }
                    }
                    val content = "你有TA喜欢的${tagsStr}${item.likeTagList.size}个标签 看TA还喜欢什么>"
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
                rvPics.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
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
                    selectPic(position, mPicsAdapter, sdv, tvPicCount)
                    val manager = rvPics.layoutManager as LinearLayoutManager
                    if (mPicsAdapter.data.size > 4) {
                        val first = manager.findFirstCompletelyVisibleItemPosition()
                        val last = manager.findLastCompletelyVisibleItemPosition()
                        if (abs(last - position) < 1) {
                            //向后滑动4个或者滑到底
                            val target = min(position + 4, mPicsAdapter.data.size - 1)
                            rvPics.scrollToPosition(target)
                        } else if (position - first < 1) {
                            //向前滑动4个或者滑到头
                            val target = max(position - 4, 0)
                            rvPics.scrollToPosition(target)
                        }
                    }

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