package com.julun.huanque.message.fragment

import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.AnimEventItemTypes.height
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.message.R

/**
 *@创建者   dong
 *@创建时间 2020/7/9 11:28
 *@描述 欢遇详情弹窗
 */
class MeetDetailFragment : BaseDialogFragment() {
    companion object {
        fun newInstance() = MeetDetailFragment()
    }

    override fun getLayoutId() = R.layout.fragment_meet_detail

    override fun initViews() {
    }

    override fun onStart() {
        super.onStart()
        setWindowConfig()
    }

    private fun setWindowConfig() {
        val window = dialog?.window ?: return
        val params = window.attributes
        params.gravity = Gravity.TOP
        params.width = window.windowManager.defaultDisplay.width - DensityHelper.dp2px(15f) * 2
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        params.y = DensityHelper.dp2px(60)
        window.attributes = params

        //不需要半透明遮罩层
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setWindowAnimations(R.style.dialog_bottom_bottom_style)
    }
}