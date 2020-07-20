package com.julun.huanque.core.ui.withdraw

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.basic.RootListData
import com.julun.huanque.common.bean.beans.WithdrawRecord
import com.julun.huanque.common.constant.WithdrawType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_withdraw_history.*
import kotlinx.android.synthetic.main.activity_withdraw_history.mRecyclerView
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


    private val mAdapter =
        object : BaseQuickAdapter<WithdrawRecord, BaseViewHolder>(R.layout.item_withdraw_history), LoadMoreModule {
            override fun convert(holder: BaseViewHolder, item: WithdrawRecord) {
                val title = if (item.type == WithdrawType.AliWithdraw) {
                    "支付宝(${item.nickname})"
                } else {
                    "微信(${item.nickname})"
                }
                holder.setText(R.id.tv_draw_title, title).setText(R.id.tv_draw_time, "提现时间：${item.time}")
                    .setText(R.id.tv_draw_money, item.money)
                val state = holder.getView<TextView>(R.id.tv_draw_state)
                when (item.status) {
                    "打款中" -> {
                        state.text = "打款中"
                        state.textColor = Color.parseColor("#FE5F63")
                    }
                    "已到账" -> {
                        state.text = "已到账"
                        state.textColor = Color.parseColor("#32CE3C")
                    }
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

    override fun initEvents(rootView: View) {
        pagerHeader.imageViewBack.onClickNew {
            finish()
        }
        mAdapter.loadMoreModule.setOnLoadMoreListener {
            logger.info("loadMoreModule 加载更多")
            mViewModel.queryInfo(QueryType.LOAD_MORE)
        }
        mRefreshView.setOnRefreshListener {
            mViewModel.queryInfo(QueryType.REFRESH)
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
    private fun refreshData(listData: RootListData<WithdrawRecord>) {

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
            NetStateType.SUCCESS -> {
                mAdapter.setEmptyView(MixedHelper.getEmptyView(this))
            }
            NetStateType.LOADING -> {
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