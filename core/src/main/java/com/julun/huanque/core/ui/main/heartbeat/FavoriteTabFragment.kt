package com.julun.huanque.core.ui.main.heartbeat

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.animation.addListener
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.FavoriteUserBean
import com.julun.huanque.common.bean.beans.HomePagePicBean
import com.julun.huanque.common.bean.beans.UserTagBean
import com.julun.huanque.common.constant.BooleanType
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.NearbyPicListAdapter
import com.julun.huanque.core.manager.VideoPlayerManager
import com.julun.huanque.core.ui.homepage.HomePageActivity
import com.julun.huanque.core.ui.tag_manager.TagUserPicsActivity
import com.julun.huanque.core.widgets.HomeCardTagView
import kotlinx.android.synthetic.main.fragment_favorite_tab.*
import org.jetbrains.anko.imageResource
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.random.Random


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/29 9:48
 *
 *@Description: ProgramTabFragment 节目单 根据tab的类型展示不同的主播
 *
 */
class FavoriteTabFragment : BaseVMFragment<FavoriteTabViewModel>() {

    companion object {
        const val BANNER_POSITION = 6
        fun newInstance(tab: UserTagBean?): FavoriteTabFragment {
            return FavoriteTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val favoriteViewModel: FavoriteViewModel by activityViewModels()

    private var currentTab: UserTagBean? = null

//    private val bottomLayout: View by lazy {
//        LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_favorite, null)
//    }

    override fun getLayoutId(): Int = R.layout.fragment_favorite_tab

    private val layoutManager: GridLayoutManager by lazy { GridLayoutManager(requireContext(), 2) }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? UserTagBean

        authorList.layoutManager = layoutManager
        initViewModel()

        authorList.adapter = authorAdapter

        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, view, position ->
            val item = authorAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            val shareView = view.findViewById<View>(R.id.card_img)
//                    val tv_user_name = parentView.findViewById<View>(R.id.tv_user_name)

            val intent = Intent(requireActivity(), HomePageActivity::class.java)
            val pair1: androidx.core.util.Pair<View, String> =
                androidx.core.util.Pair(shareView, ViewCompat.getTransitionName(shareView) ?: "")
//                    val pair2: androidx.core.util.Pair<View, String> =
//                        androidx.core.util.Pair(tv_user_name, ViewCompat.getTransitionName(tv_user_name) ?: "")

            /**
             * 4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
             */
            val activityOptionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), pair1)

            intent.putExtra(ParamConstant.UserId, item.userId)
            intent.putExtra(ParamConstant.FavoriteUserBean, item)
            startActivity(intent, activityOptionsCompat.toBundle())
        }
        authorAdapter.onAdapterChildClickNew { adapter, view, position ->
            val item=authorAdapter.getItemOrNull(position)
            when (view.id) {
                R.id.ll_top_tag -> {
                    if (item != null) {
                        TagUserPicsActivity.start(requireActivity(), item.tagId, item.userId)
                    }
                }
            }
        }
        authorList.itemAnimator = null
        authorAdapter.animationEnable = true
        authorAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInBottom)
//        authorAdapter.addFooterView(bottomLayout)
        //todo
//        mRefreshLayout.isEnabled = false
//        mRefreshLayout.setOnRefreshListener {
//            requestData(QueryType.REFRESH, currentTab?.typeCode)
//        }

//        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        mRefreshLayout.setReboundDuration(80)
        mRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            requestData(QueryType.LOAD_MORE, currentTab?.tagId)
        }

    }

    /**
     * 记录当前的正中间的view
     */
    private var currentMiddle: Int = -1

    private var loading: Boolean = false

    private var isUserDrag: Boolean = false
    private var lastY: Float = 0f

    //一次完整的触摸事件只触发一次下拉操作
    private var actionDone: Boolean = false
    override fun initEvents(rootView: View) {
        super.initEvents(rootView)
//        authorList.onTouch { _, event ->
//            when (event.action) {
//                MotionEvent.ACTION_UP -> {
//                    lastY = 0f
//                    actionDone = false
//                }
//                MotionEvent.ACTION_MOVE -> {
//                    val dY = event.y - lastY
////                    logger.info("dY=$dY  lastY=$lastY")
//                    if (lastY != 0f && dY < 0) {
//
//                        if (isUserDrag && !actionDone) {
//
//                            val totalItemCount = layoutManager.itemCount
//                            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
//                            if (!loading && totalItemCount <= lastVisibleItem + 1) {
//                                //todo 加载下一页
//                                logger.info("加载下一页")
//                                requestData(QueryType.LOAD_MORE, currentTab?.typeCode)
//                                actionDone = true
//                            }
//
//                        }
//                    }
//                    lastY = event.y
//                }
//            }
//            false
//        }

        authorList.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                private var isUp: Boolean = false
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
//                logger.info("currentState=${newState}")
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
//                    val positionFirst = layoutManager.findFirstVisibleItemPosition()
//                    val positionLast = layoutManager.findLastVisibleItemPosition()
//                    val random = Random.nextBoolean()
//                    val dif = if (random) 1 else 0
//                    if (isUp) {
//                        currentMiddle = (positionFirst + positionLast) / 2 - dif
//                    } else {
//                        currentMiddle = (positionFirst + positionLast) / 2 + 1
//                    }
//                    startPlayMidVideo()
                    }
                    isUserDrag = newState == RecyclerView.SCROLL_STATE_DRAGGING

                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    isUp = dy < 0
//                if (currentPlayView != null) {
//                    val viewRect = Rect()
//                    currentPlayView!!.getLocalVisibleRect(viewRect)
////                    logger.info("onScrolled dy=${dy} currentPlayView.top=${viewRect.top} currentPlayView.bottom=${viewRect.bottom} ")
//                    if (dy > 0) {
//                        if (viewRect.top > currentPlayView!!.height / 2) {
//                            logger.info("向上已出一半")
//                            removeItemPlay(currentPlayView!!)
//                        }
//                    } else if (dy < 0) {
//                        if (viewRect.bottom < currentPlayView!!.height / 2) {
//                            logger.info("向下已出一半")
//                            removeItemPlay(currentPlayView!!)
//                        }
//                    }
//                }

                }
            })

    }

    private fun removeItemPlay(view: View) {

        if (view.tag != null) {
            VideoPlayerManager.removePlay()
            currentPlayView = null
        }
    }

    private var currentPlayView: View? = null
    private fun startPlayMidVideo() {
        if (currentMiddle == -1) {
            return
        }
        val current = getFitPosition()
        logger.info("startPlayMidVideo:${current}  currentMiddle=$currentMiddle")
        //接着播放新的item
        val itemCurrent = authorAdapter.getItemOrNull(current)
        val content = itemCurrent
        val viewHolder = authorList.findViewHolderForAdapterPosition(current) as? BaseViewHolder?

//        if (content != null && content is NearbyUserBean && viewHolder != null) {
//            var url = GlobalUtils.getPlayUrl(content.playInfo ?: return)
//            val itemView = viewHolder.getViewOrNull<FrameLayout>(R.id.program_container) ?: return
//            if (VideoPlayerManager.startPlay(url, itemView)) {
//                currentPlayView = itemView
//            }
//        }
    }

    private fun getFitPosition(): Int {
        var doing = true
        var current = currentMiddle
//        while (doing) {
//            val itemCurrent = authorAdapter.getItemOrNull(current)
//            doing = itemCurrent != null && itemCurrent !is ProgramLiveInfo
//            if (!doing) {
//                break
//            }
//            current++
//        }
        return current
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.tagName}")
        super.onResume()
        startPlayMidVideo()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.tagName}")
        super.onPause()
        if (currentPlayView != null) {
//            removeItemPlay(currentPlayView!!)
            VideoPlayerManager.stopPlay()
        }
    }

    /**
     * 该方法供父容器在显隐变化时调用
     */
    override fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的界面开始显隐=${currentTab?.tagName} hide=$hidden")
        if (hidden) {
            if (currentPlayView != null) {
//                removeItemPlay(currentPlayView!!)
                VideoPlayerManager.stopPlay()
            }
        } else {
            startPlayMidVideo()
        }
    }

    override fun lazyLoadData() {
        requestData(QueryType.INIT, currentTab?.tagId)
    }

    private fun initViewModel() {
        if (currentTab?.tagId == -1) {
            val first = favoriteViewModel.firstListData.value?.requireT()?.list
            if (first != null) {
                mViewModel.addFirstList(first)
            }
        }
        mViewModel.dataList.observe(this, Observer {
            mRefreshLayout.finishLoadMore(50)
            if (it.state == NetStateType.SUCCESS) {
                playEndAni(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }

//            mRefreshLayout.isRefreshing = false
            loading = false
        })


//        if (currentTab?.tagId == -1) {
//            favoriteViewModel.firstListData.observe(viewLifecycleOwner, Observer {
//                if (it.isSuccess()) {
//                    mViewModel.addFirstList(it.requireT().list)
//                }
//            })
//        }


    }

    private fun loadDataFail(isPull: Boolean) {
        ToastUtils.show2("加载失败")
    }

    private fun outAnimators(view: View, index: Int): Animator {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -ScreenUtils.screenHeightFloat)
        animator.duration = 250L
        animator.startDelay = 60L * index
        animator.interpolator = DecelerateInterpolator(1.3f)
        return animator
    }

    private fun playEndAni(listData: MutableList<FavoriteUserBean>) {
        if (authorList.childCount == 0) {
            renderHotData(listData)
            return
        }
        val animationSet: AnimatorSet = AnimatorSet()
        val list = mutableListOf<Animator>()
        authorList.children.forEachIndexed { index, view ->
            list.add(outAnimators(view, index))
        }
        animationSet.playTogether(list)
        animationSet.start()
        animationSet.addListener(onEnd = {
            renderHotData(listData)
        })
    }

    private fun renderHotData(listData: MutableList<FavoriteUserBean>) {


//        if (listData.isPull) {
//            val programList = listData.list.distinct().sliceFromStart(4)
//            authorAdapter.setList(programList)
////            VideoPlayerManager.removePlay()
////            authorList.postDelayed({
////                if (totalList.size >= 4) {
////                    currentMiddle = if (Random.nextBoolean()) {
////                        2
////                    } else {
////                        3
////                    }
////                } else if (totalList.size >= 3) {
////                    currentMiddle = 2
////                } else if (totalList.size > 0) {
////                    currentMiddle = totalList.size - 1
////                } else {
////                    currentMiddle = -1
////                }
////                startPlayMidVideo()
////            }, 100)
//
//        } else {
//            val programList = listData.list.distinct().sliceFromStart(4)
//            authorAdapter.setList(programList)
//        }
        authorAdapter.setList(listData)
//        bottomLayout.bottom_title.show()
        setEmpty()

    }

    private fun setEmpty() {
        if (authorAdapter.data.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyRes = R.mipmap.icon_no_data_01,emptyTxt = "${currentTab?.tagName}没有更多人了，去看看其他的吧")
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    //
    fun scrollToTopAndRefresh() {
        authorList.scrollToPosition(0)
        authorList.postDelayed({
            requestData(QueryType.REFRESH, currentTab?.tagId)
        }, 100)
    }

    private fun requestData(queryType: QueryType, code: Int?) {
        loading = true
        mViewModel.requestProgramList(queryType, code)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
//                state_pager_view.showSuccess()
                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    requestData(QueryType.INIT, currentTab?.tagId)
                })
            }

        }

    }

    //去掉底部栏49 顶部tab=53 中间tab=40 其他边距20
    private val itemHeight = (ScreenUtils.getRealScreenHeight() - ScreenUtils.statusHeight - dp2px(49 + 53 + 40 + 20)) / 2
    private val authorAdapter: BaseQuickAdapter<FavoriteUserBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<FavoriteUserBean, BaseViewHolder>(R.layout.item_favorite_user_list) {
            init {
                addChildClickViewIds(R.id.ll_top_tag)
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
                if (viewType == 0) {
                    val cardView = holder.getView<CardView>(R.id.card_view)
                    val sdvLp = cardView.layoutParams
                    sdvLp.height = itemHeight
                }

                return holder
            }

            override fun convert(holder: BaseViewHolder, item: FavoriteUserBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.coverPic)

                val age = if (item.age != 0) {
                    "${item.age}岁"
                } else {
                    "秘密"
                }
                val sdvTag = holder.getView<SimpleDraweeView>(R.id.sdv_tag)
                sdvTag.loadImage(item.tagIcon, 16f, 16f)
                holder.setText(R.id.tv_tag, "${item.tagName} ${item.picCnt}")

                holder.setText(R.id.tv_age, age)

                val tvDistance = holder.getView<TextView>(R.id.tv_distance)
                val ivDistance = holder.getView<ImageView>(R.id.iv_distance)
                if (item.distance != -1) {
                    if (item.sameCity) {
                        tvDistance.show()
                        ivDistance.hide()
                        when {
                            item.distance < 1000 -> {
                                holder.setText(R.id.tv_distance, "${item.distance}")
                                holder.setText(R.id.tv_location, "m ${item.area}")
                            }
                            else -> {
                                val format = DecimalFormat("#.0")
                                format.roundingMode = RoundingMode.DOWN
                                val dt = format.format((item.distance / 1000.0))
                                holder.setText(R.id.tv_distance, dt)
                                holder.setText(R.id.tv_location, "km ${item.area}")
                            }
                        }
                    } else {
                        tvDistance.hide()
                        ivDistance.show()
                        holder.setText(R.id.tv_location, item.area)
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
                    holder.setText(R.id.tv_location, currentStar)
                }
                if (item.interactTips.isNotEmpty()) {
                    holder.setText(R.id.tv_top_right_tips, item.interactTips).setGone(R.id.tv_top_right_tips, false)
                } else {
                    holder.setGone(R.id.tv_top_right_tips, true)
                }
                val rvPics = holder.getView<RecyclerView>(R.id.rv_pics)
                val rvLp = rvPics.layoutParams
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
                        HorizontalItemDecoration(dp2px(4))
                    )
                }
                val list = mutableListOf<HomePagePicBean>()
                kotlin.run {
                    item.coverPicList.forEachIndexed { index, pic ->
                        if (index > 2) {
                            return@run
                        }
                        if (index == 0) {
                            list.add(HomePagePicBean(pic, selected = BooleanType.TRUE))
                        } else {
                            list.add(HomePagePicBean(pic, selected = BooleanType.FALSE))
                        }

                    }
                }
                mPicsAdapter.setList(list)
            }

            override fun startAnim(anim: Animator, index: Int) {
                anim.duration = 250L
                anim.startDelay = index * 80L
                anim.start()
            }
        }
    }
}