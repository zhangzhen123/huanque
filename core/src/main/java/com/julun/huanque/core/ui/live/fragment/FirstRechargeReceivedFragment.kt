package com.julun.huanque.core.ui.live.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.bean.beans.GiftIcon
import com.julun.huanque.common.bean.beans.SinglePack
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.StringHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.ui.web.WebActivity
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.JsonUtil
import com.julun.huanque.core.R
import com.julun.huanque.core.ui.live.adapter.FirstRechargeGiftAdapter
import kotlinx.android.synthetic.main.fragment_first_recharge.*
import kotlinx.android.synthetic.main.fragment_first_recharge_received.*
import kotlinx.android.synthetic.main.fragment_first_recharge_received.ivClose
import kotlinx.android.synthetic.main.fragment_first_recharge_received.iv_helper
import kotlinx.android.synthetic.main.fragment_first_recharge_received.iv_recharge
import kotlinx.android.synthetic.main.fragment_first_recharge_received.rvNewUserGift
import kotlinx.android.synthetic.main.fragment_first_recharge_received.tv_title

/**
 *@创建者   dong
 *@创建时间 2020/10/23 11:56
 *@描述 首充到账弹窗
 */
class FirstRechargeReceivedFragment : BaseDialogFragment() {
    companion object {
        fun newInstance(bean: SinglePack): FirstRechargeReceivedFragment {
            val fragment = FirstRechargeReceivedFragment()
            fragment.arguments = Bundle().apply { putSerializable(ParamConstant.First_Recharge_Received, bean) }
            return fragment
        }
    }

    private var mGridLayoutManager: GridLayoutManager? = null

    private val giftAdapter = FirstRechargeGiftAdapter()

    override fun getLayoutId() = R.layout.fragment_first_recharge_received

    override fun initViews() {
        val packBean = arguments?.getSerializable(ParamConstant.First_Recharge_Received) as? SinglePack
        if (packBean == null) {
            dismiss()
            return
        }


        tv_title?.mColorArray = intArrayOf(GlobalUtils.formatColor("#FFFCED"), GlobalUtils.formatColor("#FFDA03"))
        tv_title?.mPositionArray = floatArrayOf(0f, 1f)
        tv_title.text = "${packBean.money}"

        val giftList = packBean.awardDetails
        initRecyclerView(giftList.size)
        mGridLayoutManager?.spanCount = giftList.size
        giftAdapter.setList(giftList)

        ivClose.onClickNew {
            dismiss()
        }

        iv_recharge.onClickNew {
            dismiss()
        }
        iv_helper.onClickNew {
            WebActivity.startWeb(requireActivity(), StringHelper.getOssImgUrl(packBean.explainPic), "首充送豪礼")
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initRecyclerView(count: Int) {
        mGridLayoutManager = GridLayoutManager(context, count)
        rvNewUserGift.layoutManager = mGridLayoutManager
        rvNewUserGift.adapter = giftAdapter
    }

    override fun onStart() {
        super.onStart()
        setDialogSize(Gravity.CENTER, 0, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK
        }
    }
}