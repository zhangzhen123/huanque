package com.julun.huanque.core.ui.main.makefriend

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.julun.huanque.common.base.BaseViewModelFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.HeadModule
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.PhotoBean
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_make_friend.*

class MakeFriendsFragment : BaseViewModelFragment<MakeFriendsViewModel>() {

    companion object {
        fun newInstance() = MakeFriendsFragment()
    }

    private val mAdapter: MakeFriendsAdapter by lazy { MakeFriendsAdapter() }
    override fun lazyLoadData() {
        mViewModel.queryInfo()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_make_friend
    }

    override fun initEvents(rootView: View) {
        mRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        mRecyclerView.adapter = mAdapter

        mAdapter.setOnLoadMoreListener({
            mViewModel.queryInfo(QueryType.LOAD_MORE)
        }, mRecyclerView)
        mRefreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
        }
        mAdapter.setOnItemClickListener { _, _, position ->
            logger.info("点击了第几个index=$position")
            val item=mAdapter.getItem(position)?:return@setOnItemClickListener
            if(item.showType==HomeItemBean.GUIDE_TO_COMPLETE_INFORMATION){
                logger.info("跳转编辑资料页")
            }

        }
        mAdapter.mOnItemAdapterListener = object : MakeFriendsAdapter.OnItemAdapterListener {
            override fun onPhotoClick(index: Int, position: Int, item: PhotoBean) {
                logger.info("index=$index position=$position item=$item")
            }

            override fun onHeadClick(item: HeadModule?) {
                logger.info("头部分类：${item?.moduleType}")
            }

        }
        mAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.btn_action -> {
                    logger.info("点击了操作按钮--$position")
                }
                R.id.iv_audio_play -> {
                    logger.info("点击了音频播放---$position")
                }
                R.id.iv_guide_tag_close->{
                    logger.info("点击引导标签关闭---$position")
                }
                R.id.iv_guide_info_close->{
                    logger.info("点击引导完善资料关闭---$position")
                }
            }
        }
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.stateList.observe(viewLifecycleOwner, Observer {
            //
            mRefreshView.isRefreshing = false
            if (it.state == NetStateType.SUCCESS) {
                loadData(it.getT())
            } else if (it.state == NetStateType.ERROR) {
                //dodo
            }
        })


    }

    private fun loadData(stateList: RootListData<HomeItemBean>) {

        if (stateList.isPull) {
            mAdapter.replaceData(stateList.list)
        } else {
            mAdapter.addData(stateList.list)
        }

        if (stateList.hasMore) {
            mAdapter.loadMoreComplete()
        } else {
            if (stateList.isPull)
                mAdapter.loadMoreEnd(true)
            else
                mAdapter.loadMoreEnd()
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
                mAdapter.emptyView = MixedHelper.getLoadingView(requireContext())
            }
            NetStateType.ERROR -> {
                mAdapter.emptyView =
                    MixedHelper.getErrorView(ctx = requireContext(), msg = state.message, onClick = View.OnClickListener {
                        mViewModel.queryInfo(QueryType.REFRESH)
                    })
            }
            NetStateType.NETWORK_ERROR -> {
                mAdapter.emptyView =
                    MixedHelper.getErrorView(ctx = requireContext(), msg = "网络错误", onClick = View.OnClickListener {
                        mViewModel.queryInfo(QueryType.REFRESH)
                    })
            }
        }
    }

}