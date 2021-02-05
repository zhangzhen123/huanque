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
import com.julun.huanque.common.constant.CardType
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.AppHelper
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.common.widgets.recycler.decoration.HorizontalItemDecoration
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.FavoriteAdapter
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

    private val authorAdapter: FavoriteAdapter by lazy { FavoriteAdapter() }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? UserTagBean

        authorList.layoutManager = GridLayoutManager(requireContext(), 2)
        initViewModel()

        authorList.adapter = authorAdapter

        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, view, position ->
            val item = authorAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
//            val shareView = view.findViewById<View>(R.id.card_img)
////                    val tv_user_name = parentView.findViewById<View>(R.id.tv_user_name)
//
//            val intent = Intent(requireActivity(), HomePageActivity::class.java)
//            val pair1: androidx.core.util.Pair<View, String> =
//                androidx.core.util.Pair(shareView, ViewCompat.getTransitionName(shareView) ?: "")
////                    val pair2: androidx.core.util.Pair<View, String> =
////                        androidx.core.util.Pair(tv_user_name, ViewCompat.getTransitionName(tv_user_name) ?: "")
//
//            /**
//             * 4、生成带有共享元素的Bundle，这样系统才会知道这几个元素需要做动画
//             */
//            val activityOptionsCompat: ActivityOptionsCompat =
//                ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), pair1)
//
//            intent.putExtra(ParamConstant.UserId, item.userId)
//            intent.putExtra(ParamConstant.FavoriteUserBean, item)
//            startActivity(intent, activityOptionsCompat.toBundle())
            if(item.cardType== CardType.GUIDE){
                AppHelper.openTouch(item.touchType,item.touchValue,requireActivity())
            }else{
                HomePageActivity.newInstance(requireActivity(), item.userId)
            }
        }
        authorAdapter.onAdapterChildClickNew { adapter, view, position ->
            val item = authorAdapter.getItemOrNull(position)
            when (view.id) {
                R.id.ll_top_tag_holder -> {
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
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -ScreenUtils.screenHeightFloat * 0.8f)
        animator.duration = 450L
        animator.startDelay = 100L * index
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
            state_pager_view.showEmpty(emptyRes = R.mipmap.icon_no_data_01, emptyTxt = "${currentTab?.tagName}没有更多人了，去看看其他的吧")
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

}