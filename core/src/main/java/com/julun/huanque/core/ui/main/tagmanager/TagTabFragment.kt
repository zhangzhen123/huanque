package com.julun.huanque.core.ui.main.tagmanager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.DynamicItemBean
import com.julun.huanque.common.bean.beans.HomeDynamicListInfo
import com.julun.huanque.common.bean.beans.PagerTab
import com.julun.huanque.common.bean.beans.TagManagerBean
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.constant.SquareTabType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_favorite_tag_tab.*

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/11/20 17:34
 *
 *@Description: DynamicTabFragment
 *
 */
class TagTabFragment : BaseVMFragment<TagTabViewModel>() {

    companion object {
        fun newInstance(tab: PagerTab?): TagTabFragment {
            return TagTabFragment().apply {
                val bundle = Bundle()
                bundle.putSerializable(IntentParamKey.TAB_TYPE.name, tab)
                this.arguments = bundle
            }
        }
    }

    private val tagManagerViewModel: TagManagerViewModel by activityViewModels()

    private var currentTab: PagerTab? = null

    private val mAdapter: BaseQuickAdapter<DynamicItemBean, BaseViewHolder> by lazy {
        object : BaseQuickAdapter<DynamicItemBean, BaseViewHolder>(R.layout.item_favorite_tag_list), LoadMoreModule {

            override fun convert(holder: BaseViewHolder, item: DynamicItemBean) {
                val sdv = holder.getView<SimpleDraweeView>(R.id.card_img)
                sdv.loadImageNoResize(item.headPic)
                holder.setText(R.id.user_name, item.nickname)
                holder.setGone(R.id.view_fg, item.follow)
            }

        }
    }


    override fun getLayoutId(): Int = R.layout.fragment_favorite_tag_tab

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        currentTab = arguments?.getSerializable(IntentParamKey.TAB_TYPE.name) as? PagerTab
        initViewModel()
        postList.layoutManager = GridLayoutManager(requireContext(), 3)
        mAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info(" postList.stopScroll()")
            postList.stopScroll()
            logger.info("authorAdapter loadMoreModule 加载更多")
            queryData(QueryType.LOAD_MORE, currentTab?.typeCode)
        }
        postList.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(5)))
        postList.adapter = mAdapter

//        (postList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false


        mRefreshLayout.setOnRefreshListener {
            queryData(QueryType.REFRESH, currentTab?.typeCode)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)

    }

    override fun initEvents(rootView: View) {
        super.initEvents(rootView)

        mAdapter.onAdapterClickNew { _, _, position ->
            val item = mAdapter.getItemOrNull(position) ?: return@onAdapterClickNew
            item.follow = !item.follow
            mAdapter.notifyItemChanged(position)
            if (!item.follow) {
                tagManagerViewModel.addTag(
                    TagManagerBean(
                        tagName = currentTab?.typeName ?: return@onAdapterClickNew,
                        type = currentTab?.typeCode ?: return@onAdapterClickNew,
                        num = 1
                    )
                )
            } else {
                tagManagerViewModel.removeTag(
                    TagManagerBean(
                        tagName = currentTab?.typeName ?: return@onAdapterClickNew,
                        type = currentTab?.typeCode ?: return@onAdapterClickNew,
                        num = 1
                    )
                )
            }

        }
    }

    override fun onResume() {
        logger.info("onResume =${currentTab?.typeName}")
        super.onResume()
    }

    override fun onPause() {
        logger.info("onPause =${currentTab?.typeName}")
        super.onPause()
    }

    /**
     * 该方法供父容器在显隐变化时调用
     */
    override fun onParentHiddenChanged(hidden: Boolean) {
        logger.info("当前的界面开始显隐=${currentTab?.typeName} hide=$hidden")
    }

    override fun lazyLoadData() {
        queryData(QueryType.INIT, currentTab?.typeCode)
    }

    private fun queryData(queryType: QueryType, typeCode: String?) {
        mViewModel.requestPostList(queryType, typeCode)
    }

    private fun initViewModel() {
        mViewModel.dataList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderDynamicData(it.requireT())
            } else if (it.state == NetStateType.ERROR) {
                loadDataFail(it.isRefresh())
            }
            mRefreshLayout.isRefreshing = false
        })
        mViewModel.postList.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                renderDynamicGroupData(it.requireT())
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
            mAdapter.loadMoreModule.loadMoreFail()
        }
    }

    private fun renderDynamicGroupData(listData: RootListData<DynamicItemBean>) {

        if (listData.isPull) {
            val programList = listData.list.distinct()
            mAdapter.setList(programList)

        } else {
            val programList = listData.list.removeDuplicate(mAdapter.data)
//            totalList.addAll(programList)
            mAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.list.isEmpty()) {
                mAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                mAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            mAdapter.loadMoreModule.loadMoreEnd()

        }

        if (mAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            mRefreshLayout.show()
            mAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    requireContext(),
                    msg = "暂无动态，快去发布一条吧~"
                )
            )
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    private fun renderDynamicData(listData: HomeDynamicListInfo) {

        if (listData.isPull) {
            val programList = listData.postList.distinct()
            mAdapter.setList(programList)
        } else {
            val programList = listData.postList.removeDuplicate(mAdapter.data)
//            totalList.addAll(programList)
            mAdapter.addData(programList)
        }
        if (listData.hasMore) {
            //如果下拉加载更多时 返回的列表为空 会触发死循环 这里直接设置加载完毕状态
            if (listData.postList.isEmpty()) {
                mAdapter.loadMoreModule.loadMoreEnd(false)
            } else {
                mAdapter.loadMoreModule.loadMoreComplete()
            }

        } else {
            //防止底部没有边距
            mAdapter.loadMoreModule.loadMoreEnd()

        }
        if (currentTab?.typeCode == SquareTabType.RECOMMEND && mAdapter.data.isEmpty() && listData.groupList.isEmpty()) {
            mRefreshLayout.hide()
            state_pager_view.showEmpty(emptyTxt = "暂无内容")
        } else if (mAdapter.data.isEmpty()) {
            state_pager_view.showSuccess()
            mRefreshLayout.show()
            var message = "暂无内容"
            mAdapter.setEmptyView(
                MixedHelper.getEmptyView(
                    requireContext(),
                    msg = message
                )
            )
        } else {
            mRefreshLayout.show()
            state_pager_view.showSuccess()
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
//                state_pager_view.showSuccess()
//                mRefreshLayout.show()
            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    queryData(QueryType.INIT, currentTab?.typeCode)
                })
            }

        }

    }


}