package com.julun.huanque.core.ui.main.heartbeat

import android.Manifest
import android.animation.Animator
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseLazyFragment
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.bean.beans.NearbyUserBean
import com.julun.huanque.common.bean.beans.ManagerTagBean
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.constant.BusiConstant
import com.julun.huanque.common.suger.NoDoubleClickListener
import com.julun.huanque.common.suger.dp2px
import com.julun.huanque.common.suger.loadImageNoResize
import com.julun.huanque.common.suger.onAdapterClickNew
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.permission.rxpermission.RxPermissions
import com.julun.huanque.common.widgets.cardlib.CardLayoutManager
import com.julun.huanque.common.widgets.cardlib.CardSetting
import com.julun.huanque.common.widgets.cardlib.CardTouchHelperCallback
import com.julun.huanque.common.widgets.cardlib.OnSwipeCardListener
import com.julun.huanque.common.widgets.cardlib.utils.ReItemTouchHelper
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.NearbyPicListAdapter
import com.julun.huanque.core.widgets.HomeCardTagView
import com.julun.maplib.LocationService
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_nearby.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class NearbyFragment : BaseLazyFragment() {
    companion object {
        fun newInstance() = NearbyFragment()
    }

    val mViewModel: NearbyViewModel by viewModels()

    private val randomDistance: Int by lazy {
        (ScreenUtils.getScreenWidth() - dp2px(20)) / 3 - dp2px(26)
    }
    private lateinit var mReItemTouchHelper: ReItemTouchHelper
    private val list = mutableListOf<NearbyUserBean>()
    private val myTagList = mutableListOf<ManagerTagBean>()
    override fun lazyLoadData() {
        checkPermission()
    }

    override fun getLayoutId(): Int = R.layout.fragment_nearby

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
        val setting: CardSetting = object : CardSetting() {
            override fun couldSwipeOutDirection(): Int {
                return ReItemTouchHelper.LEFT or ReItemTouchHelper.RIGHT
            }

            override fun getCardRotateDegree(): Float {
                return 10f
            }

            override fun getCardTranslateDistance(): Int {
                return 15
            }

            override fun enableHardWare(): Boolean {
                return false
            }

            override fun getSwipeThreshold(): Float {
                return 0.15f
            }

            override fun getSwipeOutAnimDuration(): Int {
                return 200
            }

            override fun getStackDirection(): Int {
                return ReItemTouchHelper.DOWN
            }
        }
        setting.setSwipeListener(object : OnSwipeCardListener<NearbyUserBean> {
            override fun onSwiping(
                viewHolder: RecyclerView.ViewHolder?,
                dx: Float,
                dy: Float,
                direction: Int,
                ratio: Float
            ) {
                val holder: BaseViewHolder = viewHolder as BaseViewHolder
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
//                        Log.e("aaa", "swiping direction=right")
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

                    }
                    ReItemTouchHelper.RIGHT -> {

                    }
                }
                currentAniIndex = 0
                startPlayAni()
                checkToGetMore()
            }

            override fun onSwipedClear() {

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
        }



        mLocationService = LocationService(requireContext().applicationContext)
        mLocationService.registerListener(mLocationListener)
        mLocationService.setLocationOption(mLocationService.defaultLocationClientOption.apply {
            //                            this.setScanSpan(0)
//                            this.setIgnoreKillProcess(false)
        })

    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.isSuccess()) {
                list.addAll(it.requireT().list)
                myTagList.addAll(it.requireT().myTagList)
                cardsAdapter.notifyDataSetChanged()
                if (it.isRefresh()) {
                    //首次加载自动播放动画
                    currentAniIndex = 0
                    startPlayAni()
                }
            }
        })
    }

    private fun checkToGetMore() {
        val loc = currentLocation ?: return
        if (list.size < 10) {
            logger.info("当前的列表数目已经小于10 开始取数据")
            mViewModel.requestNearbyList(QueryType.INIT, loc.latitude, loc.longitude, loc.province, loc.city, loc.district)
        }

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

    /**
     * 检查定位权限
     */
    private fun checkPermission() {
        val rxPermissions = RxPermissions(requireActivity())
        rxPermissions
            .requestEachCombined(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .subscribe { permission ->
                when {
                    permission.granted -> {
                        logger.info("获取权限成功")
                        mLocationService.start()
                    }
                    permission.shouldShowRequestPermissionRationale -> // Oups permission denied
                        logger.info("获取定位被拒绝")
                    else -> {
                        logger.info("获取定位被永久拒绝")
                    }
                }

            }
    }

    /**
     * 停止定位
     */
    private fun stopLocation() {
        mLocationService.stop()
        mLocationService.unregisterListener(mLocationListener)
    }

    override fun onStop() {
        super.onStop()
        stopLocation()
        stopAni()
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
            val first: ManagerTagBean? = tags.getOrNull(currentAniIndex)
            currentAniIndex++
            val second: ManagerTagBean? = tags.getOrNull(currentAniIndex)
            currentAniIndex++

            if (first == null && second == null) {
                logger.info("没有数据了 停止动画")
                return@postDelayed
            }
            rd1.removeListener()
            rd2.removeListener()
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
                addChildClickViewIds(R.id.card_img)
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
                    val sameList = mutableListOf<ManagerTagBean>()
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
                sdv.loadImageNoResize(pic.pic)
                tv.text = "${position + 1}/${mPicsAdapter.data.size}"
            }
        }
    }

}