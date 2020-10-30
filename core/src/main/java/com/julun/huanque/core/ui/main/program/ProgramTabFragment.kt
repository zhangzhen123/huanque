package com.julun.huanque.core.ui.main.program

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.chad.library.adapter.base.listener.GridSpanSizeLookup
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.*
import com.julun.huanque.common.constant.*
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import com.julun.huanque.core.adapter.ProgramAdapter
import com.julun.huanque.core.ui.live.PlayerActivity
import com.julun.huanque.core.ui.main.follow.FollowViewModel
import kotlinx.android.synthetic.main.fragment_program_tab.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/10/29 9:48
 *
 *@Description: ProgramTabFragment 节目单 根据tab的类型展示不同的主播
 *
 */
class ProgramTabFragment : BaseVMFragment<ProgramTabViewModel>() {

    companion object {
        const val BANNER_POSITION = 6
        fun newInstance(tab: ProgramTab?): ProgramTabFragment {
            return ProgramTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val followViewModel: FollowViewModel by activityViewModels()

    private var currentTab: ProgramTab? = null
    override fun getLayoutId(): Int = R.layout.fragment_program_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? ProgramTab

        authorList.layoutManager = GridLayoutManager(requireContext(), 2)
        initViewModel()


        authorAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" authorList.stopScroll()")
            authorList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            mViewModel.requestProgramList(QueryType.LOAD_MORE)
        }
        authorAdapter.setGridSpanSizeLookup(GridSpanSizeLookup { gridLayoutManager, viewType, position ->
            return@GridSpanSizeLookup when (viewType) {
                ProgramItemType.BANNER -> 2
                else -> 1
            }
        })
        authorList.adapter = authorAdapter

        authorList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        authorList.isNestedScrollingEnabled = false

        authorAdapter.setOnItemClickListener { _, _, position ->
            val item = authorAdapter.getItemOrNull(position)
            val content = item?.content
            if (item != null && content is ProgramLiveInfo) {
                logger.info("跳转直播间${content.programId}")
                PlayerActivity.start(requireActivity(), programId = content.programId, prePic = content.coverPic)
            }
        }
        mRefreshLayout.setOnRefreshListener {
            mViewModel.requestProgramList(QueryType.REFRESH)
            followViewModel.requestProgramList(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    override fun lazyLoadData() {
        mViewModel.requestProgramList(QueryType.INIT)
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderHotData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
    }

    private fun loadDataFail(isPull: Boolean) {
        if (isPull) {
            ToastUtils.show2("刷新失败")
        } else {
            authorAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private val totalList: MutableList<ProgramLiveInfo> = mutableListOf()
    private fun renderHotData(listData: ProgramListInfo) {

        if (listData.isPull) {
            val programList = listData.programList.distinct()
            totalList.clear()
            totalList.addAll(programList)
            val list = mutableListOf<MultiBean>()
            programList.forEach {
                list.add(MultiBean(ProgramItemType.NORMAL, it))
            }

            if (listData.adList != null) {
                if (list.size > BANNER_POSITION) {
                    list.add(BANNER_POSITION, MultiBean(ProgramItemType.BANNER, listData.adList!!))
                } else {
                    list.add(MultiBean(ProgramItemType.BANNER, listData.adList!!))
                }
            }

            authorAdapter.setList(list)

        } else {
            val programList = listData.programList.removeDuplicate(totalList)
            totalList.addAll(programList)
            val list = mutableListOf<MultiBean>()
            programList.forEach {
                list.add(MultiBean(ProgramItemType.NORMAL, it))
            }
            authorAdapter.addData(list)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.programList.isEmpty()) {
                authorAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                authorAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            authorAdapter.loadMoreModule.loadMoreEnd()
//            if (listData.isPull) {
//                authorAdapter.loadMoreModule.loadMoreEnd(true)
//            } else {
//                authorAdapter.loadMoreModule.loadMoreEnd()
//            }

        }
        if (authorAdapter.data.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "没有开播的主播，去交友看看吧", btnTex = "前往",
                onClick = View.OnClickListener {
                    logger.info("跳转到交友")
                    ARouter.getInstance().build(ARouterConstant.MAIN_ACTIVITY)
                        .withInt(IntentParamKey.TARGET_INDEX.name, 0).navigation()
                })
        }
    }

    //
    fun scrollToTopAndRefresh() {
        authorList.smoothScrollToPosition(0)
        mViewModel.queryInfo(QueryType.REFRESH)
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.requestProgramList(QueryType.INIT)
                })
            }

        }

    }

    private val authorAdapter =ProgramAdapter()
//        object : BaseMultiItemQuickAdapter<MultiBean, BaseViewHolder>(), LoadMoreModule {
//            init {
//                addItemType(ProgramItemType.BANNER, R.layout.item_program_banner)
//                addItemType(ProgramItemType.NORMAL, R.layout.item_live_square_anchor_list)
//
//            }
//
//            override fun convert(holder: BaseViewHolder, item: MultiBean) {
//                when (holder.itemViewType) {
//                    ProgramItemType.NORMAL -> {
//                        val bean = item.content
//                        if (bean is ProgramLiveInfo) {
//                            convertNormal(holder, bean)
//                        }
//
//                    }
//                    ProgramItemType.BANNER -> {
//                        val bean = item.content
//                        if (bean is MutableList<*>) {
//                            val list = mutableListOf<AdInfoBean>()
//                            bean.forEach { ad ->
//                                if (ad is AdInfoBean) {
//                                    list.add(ad)
//                                }
//                            }
//                            convertBanner(holder, list)
//                        }
//                    }
//                }
//            }
//
//            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
//                logger("onCreateViewHolder itemViewType:$viewType")
//                val holder = super.onCreateViewHolder(parent, viewType)
//                if (viewType == ProgramItemType.BANNER) {
//                    val banner = holder.getView<BGABanner>(R.id.banner)
//                    val screenWidth = ScreenUtils.getScreenWidth() - dp2px(10) * 2
//                    //修改banner高度原来0.3022 现在0.38356
//                    val lp = banner.layoutParams
//                    lp.height = (screenWidth * 0.38356).toInt()
////                lp.width = screenWidth
//                }
//                return holder
//            }
//
//            private fun convertBanner(holder: BaseViewHolder, ads: MutableList<AdInfoBean>) {
//                val banner = holder.getView<BGABanner>(R.id.banner)
//                banner.setAdapter(bannerAdapter)
//                banner.setDelegate(bannerItemCick)
//                banner.setData(ads, null)
//                // 默认是自动轮播的，当只有一条广告数据时，就不轮播了
//                banner.setAutoPlayAble(ads.size > 1)
//            }
//
//            private fun convertNormal(holder: BaseViewHolder, item: ProgramLiveInfo) {
//                holder.setText(R.id.anchor_nickname, item.programName)
//                val textHot = holder.getView<TextView>(R.id.user_count)
//                textHot.setTFDinCdc2()
//                if (item.heatValue < 10000) {
//                    textHot.text = "${item.heatValue}"
//                    holder.setGone(R.id.user_count_w, true)
//                } else {
//                    val format = DecimalFormat("#.0")
//                    format.roundingMode = RoundingMode.HALF_UP
//                    textHot.text = "${format.format((item.heatValue / 10000.0))}"
//                    holder.setGone(R.id.user_count_w, false)
//                }
//                ImageUtils.loadImage(
//                    holder.getView(R.id.anchorPicture)
//                        ?: return, item.coverPic + BusiConstant.OSS_350, 150f, 150f
//                )
//                if (item.city.isEmpty()) {
//                    holder.setGone(R.id.anchor_city, true)
//                } else {
//                    holder.setGone(R.id.anchor_city, false)
//
//                    holder.setText(R.id.anchor_city, item.city)
//                }
//                ImageUtils.loadImageLocal(holder.getView(R.id.bg_shadow), R.mipmap.bg_shadow_home_item)
//                val sdv_pic = holder.getView<SimpleDraweeView>(R.id.sdv_pic)
//                val tv_author_status = holder.getView<TextView>(R.id.tv_author_status)
//
//                if (item.rightTopTag.isNotEmpty()) {
//                    sdv_pic.show()
//                    ImageUtils.loadImageWithHeight_2(sdv_pic, StringHelper.getOssImgUrl(item.rightTopTag), dp2px(16))
//                    tv_author_status.hide()
//                } else {
//                    sdv_pic.hide()
//                    if (item.isLiving) {
//                        holder.setVisible(R.id.tv_author_status, true).setText(R.id.tv_author_status, "直播中")
//                    } else {
//                        holder.setGone(R.id.tv_author_status, true)
//                    }
//                }
//            }
//
//        }


}