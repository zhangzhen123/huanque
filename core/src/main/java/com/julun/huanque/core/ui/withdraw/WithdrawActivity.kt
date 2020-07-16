package com.julun.huanque.core.ui.withdraw

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseVMActivity
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.bean.beans.WithdrawInfo
import com.julun.huanque.common.bean.beans.WithdrawTpl
import com.julun.huanque.common.constant.WithdrawType
import com.julun.huanque.common.suger.*
import com.julun.huanque.common.widgets.recycler.decoration.GridLayoutSpaceItemDecoration2
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.activity_withdraw.*
import org.jetbrains.anko.startActivity

/**
 *
 *@Anchor: zhangzhen
 *
 *@Date: 2020/7/16 11:44
 *
 *@Description: 提现页面
 *
 */
class WithdrawActivity : BaseVMActivity<WithdrawViewModel>() {

    private val mAdapter: WithdrawAdapter by lazy { WithdrawAdapter() }

    // 用户充值选择的面额对象
    private var mSelectItem: WithdrawTpl? = null


    override fun getLayoutId(): Int = R.layout.activity_withdraw

    override fun setHeader() {
        pagerHeader.initHeaderView(titleTxt = "提现",operateTxt = "提现记录" )
        pagerHeader.textOperation.textSize=14f
        pagerHeader.textOperation.onClickNew {
            logger.info("打开提现记录")
            startActivity<WithdrawHistoryActivity>()
        }
    }
    override fun initViews(rootView: View, savedInstanceState: Bundle?) {

        initViewModel()

        gridView.adapter = mAdapter
        gridView.layoutManager = GridLayoutManager(this, 3)
        gridView.addItemDecoration(GridLayoutSpaceItemDecoration2(dp2px(8f)))

        account_money.setTFDinAltB()

        mViewModel.queryInfo(queryType = QueryType.INIT)
    }

    private var currentWithdrawType: String = ""
    override fun initEvents(rootView: View) {

        pagerHeader.imageViewBack.onClickNew {
            finish()
        }
        //item click
        mAdapter.setOnItemClickListener { _, _, position ->
            checkItem(position)
        }

        //Wechat
        wxpay_ctr.onClick {
            checkPayType(WithdrawType.WXWithdraw)
        }

        //alipay
        alipay_ctr.onClick {
            checkPayType(WithdrawType.AliWithdraw)
        }

        btn_ensure.onClickNew {

        }


    }
    private fun checkItem(position: Int) {
        val itemInfo = mAdapter.getItemOrNull(position)?:return
        mSelectItem = itemInfo
        mAdapter.setSelection(position)
    }
    private fun checkPayType(withdrawType: String) {
        currentWithdrawType = withdrawType
        when (currentWithdrawType) {
            WithdrawType.WXWithdraw -> {
                view_bg_wx.isSelected = true
                view_bg_ali.isSelected = false

            }
            WithdrawType.AliWithdraw -> {
                view_bg_wx.isSelected = false
                view_bg_ali.isSelected = true
            }
        }
    }

    private fun initViewModel() {
        mViewModel.withdrawData.observe(this, Observer {
            if (it.state == NetStateType.SUCCESS) {
                val data = it.getT()
                refreshData(data)
            }
        })


    }

    /**
     * 刷新数据
     */
    private fun refreshData(info: WithdrawInfo) {
        withdrawMoney.text="今日奖励：${info.todayCash}元"
        totalMoney.text="累计提现：${info.totalWithdraw}元"
        mSelectItem = null
        mAdapter.setNewInstance(info.tplList)


    }


    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.LOADING -> {
                state_pager_view.showLoading()
                sv_withdraw_root.hide()
                btn_ensure.hide()
            }
            NetStateType.SUCCESS -> {
                state_pager_view.showSuccess()
                sv_withdraw_root.show()
                btn_ensure.show()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                sv_withdraw_root.hide()
                btn_ensure.hide()
                state_pager_view.showError(btnClick = View.OnClickListener {
                    mViewModel.queryInfo(QueryType.INIT)
                })
            }

        }

    }


}