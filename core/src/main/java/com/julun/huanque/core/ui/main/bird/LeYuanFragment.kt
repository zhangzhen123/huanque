package com.julun.huanque.core.ui.main.bird

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.julun.huanque.common.base.BaseVMFragment
import com.julun.huanque.common.basic.NetState
import com.julun.huanque.common.bean.beans.BirdHomeInfo
import com.julun.huanque.common.constant.IntentParamKey
import com.julun.huanque.common.suger.loadImage
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.ToastUtils
import com.julun.huanque.core.R
import kotlinx.android.synthetic.main.fragment_leyuan.*

/**
 *@创建者   dong
 *@创建时间 2020/6/29 18:06
 *@描述 乐园Fragment
 */
class LeYuanFragment : BaseVMFragment<LeYuanViewModel>() {
    companion object {
        fun newInstance() = LeYuanFragment()
    }

    var programId: Long? = null
    override fun getLayoutId() = R.layout.fragment_leyuan

    override fun initViews(rootView: View, savedInstanceState: Bundle?) {
        programId = arguments?.getLong(IntentParamKey.PROGRAM_ID.name)
        initViewModel()
        iv_bottom_03.onClickNew {
            iv_bottom_03.isEnabled = false
            mViewModel.buyBird()
        }
    }

    private fun initViewModel() {
        mViewModel.programId=programId
        mViewModel.homeInfo.observe(this, Observer {
            if (it.isSuccess()) {
                renderData(it.getT())
            }
        })

        mViewModel.buyResult.observe(this, Observer {
            iv_bottom_03.isEnabled = true
            if (it.isSuccess()) {

            } else {
                ToastUtils.show(it.error?.busiMessage)
            }
        })
        mViewModel.totalCoin.observe(this, Observer {
            if (it!=null) {
                tv_balance.text="余额${it}金币"
            }
        })
        mViewModel.coinsPerSec.observe(this, Observer {
            if (it!=null) {
                tv_balance_produce.text="${it}金币/秒"
            }
        })


    }

    private fun renderData(info: BirdHomeInfo) {
        info.functionInfo.let { fInfo->
            sdv_cai_shen.loadImage(fInfo.wealth.functionIcon,80f,80f)

            sdv_bird_niu_lang.loadImage(fInfo.cowherd.functionIcon,80f,80f)

            sdv_bird_zhi_nv.loadImage(fInfo.weaver.functionIcon,80f,80f)

            sdv_bird_hong_bao.loadImage(fInfo.redpacket.functionIcon,80f,80f)

            sdv_bird_shen_mi.loadImage(fInfo.mystical.functionIcon,80f,80f)
        }

        tv_bird_price.text = "${info.unlockUpgrade.upgradeCoins}"
        tv_price_level.text = "Lv.${info.unlockUpgrade.upgradeLevel}"
        sdv_bottom_bird.loadImage(info.unlockUpgrade.upgradeIcon, 86f, 86f)
    }

    override fun showLoadState(state: NetState) {
    }

    override fun lazyLoadData() {
        mViewModel.queryHome()
    }

}