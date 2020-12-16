package com.julun.huanque.core.ui.main.heartbeat

import android.animation.Animator
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.ProgramListInfo
import com.julun.huanque.common.bean.beans.ProgramLiveInfo
import com.julun.huanque.common.bean.beans.ProgramTab
import com.julun.huanque.common.constant.ARouterConstant
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.MainPageIndexConst
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.cardlib.utils.ReItemTouchHelper
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.manager.VideoPlayerManager
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.widgets.DispatchRecyclerView
import kotlinx.android.synthetic.main.fragment_leyuan.*
import kotlinx.android.synthetic.main.fragment_program_tab.*
import kotlinx.android.synthetic.main.layout_bottom_favorite.view.*


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
        fun newInstance(tab: ProgramTab?): FavoriteTabFragment {
            return FavoriteTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private var currentTab: ProgramTab? = null

    private val bottomLayout: View by lazy {
        LayoutInflater.from(requireContext()).inflate(R.layout.layout_bottom_favorite, null)
    }

    override fun getLayoutId(): Int = R.layout.fragment_program_tab

    private val layoutManager: GridLayoutManager by lazy { GridLayoutManager(requireContext(), 2) }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? ProgramTab

        authorList.layoutManager = layoutManager
        initViewModel()

        authorList.adapter = authorAdapter

        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, _, position ->
            val item = authorAdapter.getItemOrNull(position) ?: return@setOnItemClickListener
            logger.info("跳转直播间${item.programId}")
            PlayerActivity.start(requireActivity(), programId = item.programId, prePic = item.coverPic)
        }
        authorAdapter.animationEnable = true
        authorAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInBottom)
        authorAdapter.addFooterView(bottomLayout)
        //todo
        mRefreshLayout.isEnabled = false
        mRefreshLayout.setOnRefreshListener {
            requestData(QueryType.REFRESH, currentTab?.typeCode)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

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
        authorList.onTouch { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    lastY = 0f
                    actionDone = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dY = event.y - lastY
//                    logger.info("dY=$dY  lastY=$lastY")
                    if (lastY != 0f && dY < 0) {

                        if (isUserDrag && !actionDone) {

                            val totalItemCount = layoutManager.itemCount
                            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                            if (!loading && totalItemCount <= lastVisibleItem + 1) {
                                //todo 加载下一页
                                logger.info("加载下一页")
                                requestData(QueryType.LOAD_MORE, currentTab?.typeCode)
                                actionDone = true
                            }

                        }
                    }
                    lastY = event.y
                }
            }
            false
        }

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

        if (content != null && content is ProgramLiveInfo && viewHolder != null) {
            var url = GlobalUtils.getPlayUrl(content.playInfo ?: return)
            val itemView = viewHolder.getViewOrNull<FrameLayout>(R.id.program_container) ?: return
            if (VideoPlayerManager.startPlay(url, itemView)) {
                currentPlayView = itemView
            }
        }
    }

    private fun getFitPosition(): Int {
        var doing = true
        var current = currentMiddle
        while (doing) {
            val itemCurrent = authorAdapter.getItemOrNull(current)
            doing = itemCurrent != null && itemCurrent !is ProgramLiveInfo
            if (!doing) {
                break
            }
            current++
        }
        return current
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.typeName}")
        super.onResume()
        startPlayMidVideo()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.typeName}")
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
        logger.info("当前的界面开始显隐=${currentTab?.typeName} hide=$hidden")
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
        requestData(QueryType.INIT, currentTab?.typeCode)
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderHotData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
            loading = false
        })
    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            bottomLayout.bottom_title.show()
        }
    }

    private fun renderHotData(listData: ProgramListInfo) {

        if (listData.isPull) {
            val programList = listData.programList.distinct().sliceFromStart(4)
            authorAdapter.setList(programList)
//            VideoPlayerManager.removePlay()
//            authorList.postDelayed({
//                if (totalList.size >= 4) {
//                    currentMiddle = if (Random.nextBoolean()) {
//                        2
//                    } else {
//                        3
//                    }
//                } else if (totalList.size >= 3) {
//                    currentMiddle = 2
//                } else if (totalList.size > 0) {
//                    currentMiddle = totalList.size - 1
//                } else {
//                    currentMiddle = -1
//                }
//                startPlayMidVideo()
//            }, 100)

        } else {
            val programList = listData.programList.distinct().sliceFromStart(4)
            authorAdapter.setList(programList)
        }
        bottomLayout.bottom_title.show()
        setEmpty()
    }

    private fun setEmpty() {
        if (authorAdapter.data.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "没有开播的主播，去交友看看吧", btnTex = "前往",
                onClick = View.OnClickListener {
                    logger.info("跳转到交友")
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, MainPageIndexConst.MAIN_FRAGMENT_INDEX).navigation()
                })
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    //
    fun scrollToTopAndRefresh() {
        authorList.scrollToPosition(0)
        authorList.postDelayed({
            requestData(QueryType.REFRESH, currentTab?.typeCode)
        }, 100)
    }

    private fun requestData(queryType: QueryType, code: String?) {
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
                    requestData(QueryType.INIT, currentTab?.typeCode)
                })
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val authorAdapter: BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<ProgramLiveInfo, BaseViewHolder>(R.layout.item_favorite_user_list) {

            override fun convert(holder: BaseViewHolder, item: ProgramLiveInfo) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.coverPic)
                holder.setText(R.id.user_name, item.programName)
            }

            override fun startAnim(anim: Animator, index: Int) {
                anim.startDelay = index * 100L
                anim.start()
            }
        }
    }
}