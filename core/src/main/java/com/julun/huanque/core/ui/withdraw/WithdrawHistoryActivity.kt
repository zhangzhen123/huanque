package com.julun.huanque.core.ui.withdraw

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_withdraw_history.*
import org.jetbrains.anko.textColor


/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 11:44
 *
 *@Description: 提现记录页面
 *
 */
class WithdrawHistoryActivity : BaseVMActivity<WithdrawHistoryViewModel>() {


    private val mAdapter = object : BaseQuickAdapter<Any, BaseViewHolder>(R.layout.item_withdraw_history) {
        override fun convert(holder: BaseViewHolder, item: Any) {
            holder.setText(R.id.tv_draw_title,"支付宝（xxx）").setText(R.id.tv_draw_time,"提现时间：20202020-2-2")
                .setText(R.id.tv_draw_money,"100元")
            val state=holder.getView<TextView>(R.id.tv_draw_state)
            if(holder.adapterPosition%2==0){
                state.text="已到账"
                state.textColor= Color.parseColor("#32CE3C")
            }else{
                state.text="打款中"
                state.textColor= Color.parseColor("#FE5F63")
            }


        }
    }



    override fun getLayoutId(): Int = R.layout.activity_withdraw_history

    override fun setHeader() {
        pagerHeader.initHeaderView(titleTxt = "提现记录")
    }

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initViewModel()

        mRecyclerView.adapter = mAdapter
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mViewModel.queryInfo(queryType = QueryType.INIT)
    }

    private var currentWithdrawType: String = ""
    override fun initEvents(rootView: View) {
        pagerHeader.imageViewBack.onClickNew {
            finish()
        }
    }

    private fun initViewModel() {
        mViewModel.historyData.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                refreshData(data)
            }
        })


    }

    /**
     * 刷新数据
     */
    private fun refreshData(listData: RootListData<Any>) {

        if (listData.isPull) {
            mAdapter.setList(listData.list)
        } else {
            mAdapter.addData(listData.list)
        }

        if (listData.hasMore) {
//            mAdapter.loadMoreComplete()
            mAdapter.loadMoreModule.loadMoreComplete()
        } else {
            if (listData.isPull) {
//                mAdapter.loadMoreEnd(true)
                mAdapter.loadMoreModule.loadMoreEnd(true)
            } else {
//                mAdapter.loadMoreEnd()
                mAdapter.loadMoreModule.loadMoreEnd()
            }

        }


    }


    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
            }
            NetStateType.LOADING -> {//showLoading()
                mAdapter.setEmptyView(MixedHelper.getLoadingView(this))
            }
            NetStateType.ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = state.message,
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
            NetStateType.NETWORK_ERROR -> {
                mAdapter.setEmptyView(
                    MixedHelper.getErrorView(
                        ctx = this,
                        msg = "网络错误",
                        onClick = View.OnClickListener {
                            mViewModel.queryInfo(QueryType.INIT)
                        })
                )

            }
        }

    }


}