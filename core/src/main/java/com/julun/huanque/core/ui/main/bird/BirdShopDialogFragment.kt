package com.julun.huanque.core.ui.main.bird

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseVMDialogFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.basic.NetStateType
import com.julun.huanque.common.basic.QueryType
import com.julun.huanque.common.helper.MixedHelper
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.hide
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.suger.show
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_bird_shop.*
import kotlinx.android.synthetic.main.fragment_bird_shop.ivClose
import kotlinx.android.synthetic.main.fragment_bird_shop.mRefreshLayout
import kotlinx.android.synthetic.main.fragment_bird_shop.state_pager_view
import kotlinx.android.synthetic.main.fragment_bird_tasks.*
import org.jetbrains.anko.backgroundColor

/**
 * [leYuanViewModel]将主面板的viewModel传过来 供商店调用
 */
class BirdShopDialogFragment(private val leYuanViewModel: LeYuanViewModel) : BaseVMDialogFragment<BirdShopViewModel>() {


    private val birdAdapter: ShopBirdAdapter by lazy { ShopBirdAdapter() }
    override fun getLayoutId(): Int {
        return R.layout.fragment_bird_shop
    }

    override fun configDialog() {
        setDialogSize(width = ViewGroup.LayoutParams.MATCH_PARENT, height = 480)
    }
    override fun initViews() {

        initViewModel()
        birdsList.layoutManager = GridLayoutManager(requireContext(), 3)
        birdsList.adapter = birdAdapter
        birdAdapter.setOnItemClickListener { _, _, position ->
            val item = birdAdapter.getItemOrNull(position)
            if (item != null) {
                if (item.unlocked) {
                    leYuanViewModel.buyBird(item.upgradeLevel)
                } else {
                    ToastUtils.show("当前等级未解锁！")
                }

            }
        }
        ivClose.onClickNew {
            dismiss()
        }
        state_pager_view.backgroundColor = Color.TRANSPARENT
        mRefreshLayout.setOnRefreshListener {
            mViewModel.queryShop(QueryType.REFRESH)
        }
        MixedHelper.setSwipeRefreshStyle(mRefreshLayout)
        mViewModel.queryShop()
    }

    override fun reCoverView() {
        initViewModel()
        mViewModel.queryShop()
    }

    private fun initViewModel() {
        leYuanViewModel.totalCoin.observe(this, Observer {
            totalCoin.text = "${StringHelper.formatBigNum(it)}"
        })
        mViewModel.shopInfo.observe(this, Observer {
            mRefreshLayout.isRefreshing = false
            if (it.isSuccess()) {
                birdAdapter.setNewInstance(it.requireT().upgradeList)
            }

        })

        leYuanViewModel.buyResult.observe(this, Observer {
            if (it.isSuccess()) {
                val bird = it.requireT()
                if (bird.hasEnough == true && bird.unlockUpgrade != null) {
                    val index = birdAdapter.data.indexOfFirst { item ->
                        item.upgradeLevel == bird.unlockUpgrade!!.upgradeLevel
                    }
                    if (index >= 0) {
                        logger.info("匹配到刷新的鸟=${index}")
                        birdAdapter.data.getOrNull(index)?.upgradeCoins = bird.unlockUpgrade!!.upgradeCoins
                        birdAdapter.notifyItemChanged(index)
                    }
                }

            }
        })

    }

    private fun locationToCanBuyMaxLevel() {
        val index = birdAdapter.data.indexOfFirst { !it.unlocked } - 1
        if (index > 0 && birdAdapter.itemCount > 0) {
            birdsList.scrollToPosition(index)
        }
    }

    override fun showLoadState(state: NetState) {
        when (state.state) {
            NetStateType.SUCCESS -> {//showSuccess()
                state_pager_view.showSuccess()
                mRefreshLayout.show()
                birdAdapter.setEmptyView(
                    MixedHelper.getEmptyView(
                        requireContext()
                    )
                )
                //第一次定位到最高可买
                locationToCanBuyMaxLevel()

            }
            NetStateType.LOADING -> {//showLoading()
                mRefreshLayout.hide()
                state_pager_view.showLoading()
            }
            NetStateType.ERROR, NetStateType.NETWORK_ERROR -> {
                state_pager_view.showError(showBtn = true, btnClick = View.OnClickListener {
                    mViewModel.queryShop()
                })
            }
        }
    }
}