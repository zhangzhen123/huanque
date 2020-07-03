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
import com.julun.huanque.common.basic.RootListLiveData
import com.julun.huanque.common.bean.beans.HomeItemBean
import com.julun.huanque.common.bean.beans.PhotoBean
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
        }
        mAdapter.mOnItemAdapterListener=object :MakeFriendsAdapter.OnItemAdapterListener{
            override fun onListClick(index: Int, position: Int, item: PhotoBean) {
                logger.info("index=$index position=$position item=$item")
            }

        }
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        initViewModel()
    }

    private fun initViewModel() {
        mViewModel.stateList.observe(viewLifecycleOwner, Observer {
            //
            mRefreshView.isRefreshing=false
            if(it.state==NetStateType.SUCCESS){
                loadData(it.getT())
            }else if(it.state==NetStateType.ERROR){
                //dodo
            }
        })


    }

    private fun loadData(stateList: RootListLiveData<HomeItemBean>) {

        if (stateList.isPull) {
            mAdapter.replaceData(stateList.list)
        } else {
            //合并、去重加排序(从大到小)
//            val list = adapter.data.mergeNoDuplicateNew(rankList.list).sortedByDescending {
//                it.score
//            }
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
            }
            NetStateType.ERROR -> {
//                showError(it.message)
            }
            NetStateType.NETWORK_ERROR -> {
//                showNetError()
            }
        }
    }

}