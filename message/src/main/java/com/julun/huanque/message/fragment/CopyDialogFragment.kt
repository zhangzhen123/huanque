package com.julun.huanque.message.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.julun.huanque.common.base.BaseDialogFragment
import com.julun.huanque.common.constant.ParamConstant
import com.julun.huanque.common.helper.DensityHelper
import com.julun.huanque.common.suger.onClickNew
import com.julun.huanque.common.utils.GlobalUtils
import com.julun.huanque.common.utils.ScreenUtils
import com.julun.huanque.message.R
import kotlinx.android.synthetic.main.fragment_copy.*
import kotlinx.android.synthetic.main.item_header_conversions.*
import org.jetbrains.anko.dip

/**
 *@创建者   dong
 *@创建时间 2020/7/23 17:41
 *@描述 文本消息长按复制按钮
 */
class CopyDialogFragment : BaseDialogFragment() {
    private var mLocation: IntArray? = null

    companion object {
        fun newInstance(content: String, location: IntArray): CopyDialogFragment {
            val fragment = CopyDialogFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(ParamConstant.CONTENT, content)
                putIntArray(ParamConstant.LOCATION, location)
            }
            return fragment
        }

    }

    override fun getLayoutId() = R.layout.fragment_copy

    override fun initViews() {
        mLocation = arguments?.getIntArray(ParamConstant.LOCATION)
        val content = arguments?.getString(ParamConstant.CONTENT) ?: ""
        root_view.onClickNew {
            GlobalUtils.copyToSharePlate(requireActivity(), content,"")
            dismiss()
        }
    }

    override fun needEnterAnimation() = false
    override fun configDialog() {
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        mLocation?.let { location ->
            if (location.size >= 4) {
                val window = dialog?.window ?: return
                val dX = location[0] + (location[2] - DensityHelper.dp2px(80)) / 2
                val params = window.attributes
                params.gravity = Gravity.BOTTOM or Gravity.LEFT
                params.width = DensityHelper.dp2px(80)
                params.height = DensityHelper.dp2px(34)
                params.x = dX
                params.y = ScreenUtils.getScreenHeightHasVirtualKey() - ScreenUtils.getNavigationBarHeightIfRoom(requireActivity()) - location[1]
                window.attributes = params
            }
        }
    }
}